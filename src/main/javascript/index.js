const fs = require('fs');
const path = require('path');
// I will add compare-versions to package.json in a later step.
const { compareVersions } = require('compare-versions');

const { core } = require('@actions/core');

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

function nextSnapshot(release, refdocUrlTemplate, apidocUrlTemplate) {
    const parts = release.version.split(/[.-]/);
    const major = parseInt(parts[0], 10);
    const minor = parseInt(parts[1], 10);
    let patch = parseInt(parts[2], 10);
    if (!release.version.endsWith("-SNAPSHOT")) {
        patch += 1;
    }
    const nextVersion = `${major}.${minor}.${patch}-SNAPSHOT`;
    const refdocUrl = refdocUrlTemplate.replace('{version}', nextVersion);
    const apidocUrl = apidocUrlTemplate.replace('{version}', nextVersion);
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

function syncReleases(latestRelease, documentationPath, refdocUrlTemplate, apidocUrlTemplate) {
    let releases = [];
    try {
        releases = JSON.parse(fs.readFileSync(documentationPath, 'utf-8'));
    } catch (error) {
        if (error.code !== 'ENOENT') {
            console.error(`Error reading ${documentationPath}:`, error);
            process.exit(1);
        }
    }

    const filteredReleases = releases.filter(r => !isSameMajorMinor(latestRelease, r));

    const snapshot = nextSnapshot(latestRelease, refdocUrlTemplate, apidocUrlTemplate);
    const newReleases = [latestRelease, snapshot, ...filteredReleases];

    newReleases.sort((a, b) => compareVersions(b.version, a.version));

    const markedReleases = markCurrent(newReleases);

    try {
        fs.writeFileSync(documentationPath, JSON.stringify(markedReleases, null, 2) + '\n');
        console.log(`Successfully updated ${documentationPath}`);
    } catch (error) {
        console.error(`Error writing to ${documentationPath}:`, error);
        process.exit(1);
    }
}

function main() {
    const args = {
        releases: {
            project: {
                slug: core.getInput("project-slug"),
                version: core.getInput("version"),
                apidoc: {
                    url: core.getInput("api-doc-url")
                },
                refdoc: {
                    url: core.getInput("ref-doc-url"),
                    antora: core.getBooleanInput("is-antora") // automatically parses true/false
                }
            }
        }
    };

    if (!args.releases || !args.releases.project) {
        console.error("Invalid arguments. Expected format: --releases.project.slug=... --releases.project.version=... etc.");
        process.exit(1);
    }

    const project = args.releases.project;

    if (project.version.endsWith("-SNAPSHOT")) {
        console.error("Please specify a non-SNAPSHOT release version to publish; it's accompanying SNAPSHOT version will also be published");
        process.exit(1);
    }

    const isAntora = project.refdoc.antora === 'true' || project.refdoc.antora === true;
    const slug = project.slug;

    const documentationLocation = `spring-website-content/project/${slug}`;
    const documentationPath = path.join(documentationLocation, 'documentation.json');

    const refdocUrlTemplate = project.refdoc.url.replace(/{project}|{slug}/g, slug);
    const apidocUrlTemplate = project.apidoc.url.replace(/{project}|{slug}/g, slug);

    const refdocUrl = refdocUrlTemplate.replace('{version}', project.version);
    const apidocUrl = apidocUrlTemplate.replace('{version}', project.version);

    const latestRelease = fromVersion(project.version, isAntora, refdocUrl, apidocUrl);

    try {
        fs.mkdirSync(documentationLocation, { recursive: true });
    } catch (error) {
        console.error(`Error creating directory ${documentationLocation}:`, error);
        process.exit(1);
    }

    syncReleases(latestRelease, documentationPath, refdocUrlTemplate, apidocUrlTemplate);
}

if (require.main === module) {
    main();
}

module.exports = {
    fromVersion,
    nextSnapshot,
    isSameMajorMinor,
    markCurrent,
    syncReleases
};
