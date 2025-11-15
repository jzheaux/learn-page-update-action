const fs = require('fs');
const path = require('path');
const core = require('@actions/core');
const { compareVersions } = require('compare-versions');

function fromVersion(version, isAntora, referenceDocUrl, apiDocUrl) {
    let status;
    if (!version.includes("-")) {
        status = "GENERAL_AVAILABILITY";
    } else if (version.endsWith("-SNAPSHOT")) {
        status = "SNAPSHOT";
    } else {
        status = "PRERELEASE";
    }
    return { version, isAntora, referenceDocUrl, apiDocUrl, status };
}

function nextSnapshot(release, refdocUrl, apidocUrl) {
    const parts = release.version.split(/[.-]/);
    const major = parseInt(parts[0], 10);
    const minor = parseInt(parts[1], 10);
    let patch = parseInt(parts[2], 10);
    if (!release.version.endsWith("-SNAPSHOT")) {
        patch += 1;
    }
    const nextVersion = `${major}.${minor}.${patch}-SNAPSHOT`;
    return fromVersion(nextVersion, release.isAntora, refdocUrl, apidocUrl);
}

function isSameMajorMinor(release, other) {
    const parts = release.version.split(/[.-]/);
    const otherParts = other.version.split(/[.-]/);
    return parts[0] === otherParts[0] && parts[1] === otherParts[1];
}

function markCurrent(releases) {
    let foundCurrent = false;
    return releases.map(release => {
        const newRelease = { ...release };
        if (newRelease.status === "GENERAL_AVAILABILITY" && !foundCurrent) {
            newRelease.current = true;
            foundCurrent = true;
        } else {
            newRelease.current = false;
        }
        return newRelease;
    });
}

function syncReleases(latestRelease, documentationPath, refdocUrl, apidocUrl) {
    let releases = [];
    try {
        releases = JSON.parse(fs.readFileSync(documentationPath, 'utf-8'));
    } catch (error) {
        if (error.code !== 'ENOENT') {
            core.setFailed(`Error reading ${documentationPath}: ${error}`);
            return;
        }
    }

    const filteredReleases = releases.filter(r => !isSameMajorMinor(latestRelease, r));

    const snapshot = nextSnapshot(latestRelease, refdocUrl, apidocUrl);
    const newReleases = [latestRelease, snapshot, ...filteredReleases];

    newReleases.sort((a, b) => compareVersions(b.version, a.version));

    const markedReleases = markCurrent(newReleases);

    try {
        fs.writeFileSync(documentationPath, JSON.stringify(markedReleases, null, 2) + '\n');
        console.log(`Successfully updated ${documentationPath}`);
    } catch (error) {
        core.setFailed(`Error writing to ${documentationPath}: ${error}`);
    }
}

function main() {
    const version = core.getInput('version', { required: true });
    const slug = core.getInput('project-slug', { required: true });
    const isAntora = core.getBooleanInput('is-antora', { required: true });
    const refDocUrlTemplate = core.getInput('ref-doc-url', { required: true });
    const apiDocUrlTemplate = core.getInput('api-doc-url', { required: true });

    if (version.endsWith("-SNAPSHOT")) {
        core.setFailed("Please specify a non-SNAPSHOT release version to publish; it's accompanying SNAPSHOT version will also be published");
        return;
    }

    const documentationLocation = `spring-website-content/project/${slug}`;
    const documentationPath = path.join(documentationLocation, 'documentation.json');

    const refdocUrl = refDocUrlTemplate.replace(/{project}|{slug}/g, slug);
    const apidocUrl = apiDocUrlTemplate.replace(/{project}|{slug}/g, slug);

    const latestRelease = fromVersion(version, isAntora, refdocUrl, apidocUrl);

    try {
        fs.mkdirSync(documentationLocation, { recursive: true });
    } catch (error) {
        core.setFailed(`Error creating directory ${documentationLocation}: ${error}`);
        return;
    }

    syncReleases(latestRelease, documentationPath, refdocUrl, apidocUrl);
}

if (require.main === module) {
    main();
}

module.exports = {
    fromVersion,
    nextSnapshot,
    isSameMajorMinor,
    markCurrent,
    syncReleases,
    main
};
