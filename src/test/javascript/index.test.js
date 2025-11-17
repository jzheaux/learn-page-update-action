const fs = require('fs');
const path = require('path');
const {
    fromVersion,
    nextSnapshot,
    isSameMajorMinor,
    markCurrent,
    syncReleases
} = require('../../main/javascript/index');

describe('Release Script', () => {

    describe('fromVersion', () => {
        it('should create a GENERAL_AVAILABILITY release', () => {
            const release = fromVersion("1.2.3", true, "ref-url", "api-url");
            expect(release).toEqual({
                version: "1.2.3",
                isAntora: true,
                referenceDocUrl: "ref-url",
                apiDocUrl: "api-url",
                status: "GENERAL_AVAILABILITY"
            });
        });

        it('should create a PRERELEASE release', () => {
            const release = fromVersion("1.2.3-RC1", true, "ref-url", "api-url");
            expect(release).toEqual({
                version: "1.2.3-RC1",
                isAntora: true,
                referenceDocUrl: "ref-url",
                apiDocUrl: "api-url",
                status: "PRERELEASE"
            });
        });

        it('should create a SNAPSHOT release', () => {
            const release = fromVersion("1.2.3-SNAPSHOT", true, "ref-url", "api-url");
            expect(release).toEqual({
                version: "1.2.3-SNAPSHOT",
                isAntora: true,
                referenceDocUrl: "ref-url",
                apiDocUrl: "api-url",
                status: "SNAPSHOT"
            });
        });
    });

    describe('nextSnapshot', () => {
        it('should create the next snapshot version', () => {
            const release = fromVersion("1.2.3", true, "ref-url", "api-url");
            const snapshot = nextSnapshot(release, "ref-url-{version}", "api-url-{version}");
            expect(snapshot).toEqual({
                version: "1.2.4-SNAPSHOT",
                isAntora: true,
                referenceDocUrl: "ref-url-{version}",
                apiDocUrl: "api-url-{version}",
                status: "SNAPSHOT"
            });
        });
    });

    describe('isSameMajorMinor', () => {
        it('should return true for same major and minor versions', () => {
            const release1 = { version: "1.2.3" };
            const release2 = { version: "1.2.4" };
            expect(isSameMajorMinor(release1, release2)).toBe(true);
        });

        it('should return false for different major versions', () => {
            const release1 = { version: "1.2.3" };
            const release2 = { version: "2.2.3" };
            expect(isSameMajorMinor(release1, release2)).toBe(false);
        });

        it('should return false for different minor versions', () => {
            const release1 = { version: "1.2.3" };
            const release2 = { version: "1.3.3" };
            expect(isSameMajorMinor(release1, release2)).toBe(false);
        });
    });

    describe('markCurrent', () => {
        it('should mark the latest GA release as current', () => {
            const releases = [
                { version: "1.2.3", status: "GENERAL_AVAILABILITY" },
                { version: "1.2.2", status: "GENERAL_AVAILABILITY" },
                { version: "1.3.0-SNAPSHOT", status: "SNAPSHOT" }
            ];
            const marked = markCurrent(releases);
            expect(marked).toEqual([
                { version: "1.2.3", status: "GENERAL_AVAILABILITY", current: true },
                { version: "1.2.2", status: "GENERAL_AVAILABILITY", current: false },
                { version: "1.3.0-SNAPSHOT", status: "SNAPSHOT", current: false }
            ]);
        });
    });

    describe('syncReleases', () => {
        const documentationPath = path.join(__dirname, 'documentation.json');

        beforeEach(() => {
            if (fs.existsSync(documentationPath)) {
                fs.unlinkSync(documentationPath);
            }
        });

        afterEach(() => {
            if (fs.existsSync(documentationPath)) {
                fs.unlinkSync(documentationPath);
            }
        });

        it('should create a new documentation.json file with the latest release and snapshot', () => {
            const latestRelease = fromVersion("2.0.0", true, "ref-{version}", "api-{version}");
            syncReleases(latestRelease, documentationPath, "ref-{version}", "api-{version}");

            const content = JSON.parse(fs.readFileSync(documentationPath, 'utf-8'));
            expect(content).toEqual([
                {
                    version: "2.0.1-SNAPSHOT",
                    isAntora: true,
                    referenceDocUrl: "ref-{version}",
                    apiDocUrl: "api-{version}",
                    status: "SNAPSHOT",
                    current: false
                },
                {
                    version: "2.0.0",
                    isAntora: true,
                    referenceDocUrl: "ref-{version}",
                    apiDocUrl: "api-{version}",
                    status: "GENERAL_AVAILABILITY",
                    current: true
                }
            ]);
        });

        it('should update an existing documentation.json file', () => {
            const existingReleases = [
                { version: "1.0.0", status: "GENERAL_AVAILABILITY", current: true }
            ];
            fs.writeFileSync(documentationPath, JSON.stringify(existingReleases, null, 2));

            const latestRelease = fromVersion("2.0.0", true, "ref-{version}", "api-{version}");
            syncReleases(latestRelease, documentationPath, "ref-{version}", "api-{version}");

            const content = JSON.parse(fs.readFileSync(documentationPath, 'utf-8'));
            expect(content).toEqual([
                {
                    version: "2.0.1-SNAPSHOT",
                    isAntora: true,
                    referenceDocUrl: "ref-{version}",
                    apiDocUrl: "api-{version}",
                    status: "SNAPSHOT",
                    current: false
                },
                {
                    version: "2.0.0",
                    isAntora: true,
                    referenceDocUrl: "ref-{version}",
                    apiDocUrl: "api-{version}",
                    status: "GENERAL_AVAILABILITY",
                    current: true
                },
                {
                    version: "1.0.0",
                    status: "GENERAL_AVAILABILITY",
                    current: false
                }
            ]);
        });
    });
});
