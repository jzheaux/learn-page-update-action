/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.github.actions.releasesapisync.releases;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.apache.maven.artifact.versioning.ComparableVersion;

/**
 * Client interface for the Project Service API releases resource.
 *
 * @author Josh Cummings
 */
public interface ReleasesService {

	Collection<ReleaseRead> getReleases();

	void putRelease(ReleaseWrite release);

	void deleteRelease(String release);

	default void createReleases(ReleaseWrite... releases) {
		Arrays.stream(releases).forEach(this::putRelease);
	}

	default void deleteReleasesByGeneration(ReleaseWrite release) {
		getReleases().stream().filter(release::isSameMajorMinor).map(ReleaseRead::version).forEach(this::deleteRelease);
	}

	default void syncReleases(ReleaseWrite latest) {
		deleteReleasesByGeneration(latest);
		createReleases(latest, latest.nextSnapshot());
	}

	record ReleaseRead(String version, boolean isAntora, String referenceDocUrl, String apiDocUrl, Status status,
			boolean current) {
		static ReleaseRead asCurrent(ReleaseWrite newest) {
			return new ReleaseRead(newest.version(), newest.isAntora(), newest.referenceDocUrl(), newest.apiDocUrl(),
					newest.status(), true);
		}

		static ReleaseRead notAsCurrent(ReleaseWrite newest) {
			return new ReleaseRead(newest.version(), newest.isAntora(), newest.referenceDocUrl(), newest.apiDocUrl(),
					newest.status(), false);
		}

	}

	record ReleaseWrite(String version, boolean isAntora, String referenceDocUrl, String apiDocUrl,
			Status status) implements Comparable<ReleaseWrite> {
		public static ReleaseWrite fromVersion(String version, boolean isAntora, String referenceDocUrl,
				String apiDocUrl) {
			if (!version.contains("-")) {
				return new ReleaseWrite(version, isAntora, referenceDocUrl, apiDocUrl, Status.GENERAL_AVAILABILITY);
			}
			if (version.endsWith("-SNAPSHOT")) {
				return new ReleaseWrite(version, isAntora, referenceDocUrl, apiDocUrl, Status.SNAPSHOT);
			}
			return new ReleaseWrite(version, isAntora, referenceDocUrl, apiDocUrl, Status.PRERELEASE);
		}

		boolean isSameMajorMinor(ReleaseRead other) {
			String[] parts = this.version.split("[\\.\\-]");
			String[] otherParts = other.version().split("[\\.\\-]");
			return parts[0].equals(otherParts[0]) && parts[1].equals(otherParts[1]);
		}

		ReleaseWrite nextSnapshot() {
			String[] parts = this.version.split("[\\.\\-]");
			int major = Integer.parseInt(parts[0]);
			int minor = Integer.parseInt(parts[1]);
			int patch = Integer.parseInt(parts[2]);
			boolean isSnapshot = this.version.endsWith("SNAPSHOT");
			if (!isSnapshot) {
				patch += 1;
			}
			String nextVersion = major + "." + minor + "." + patch + "-SNAPSHOT";
			return ReleaseWrite.fromVersion(nextVersion, this.isAntora, this.referenceDocUrl, this.apiDocUrl);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ReleaseRead that)) {
				return false;
			}
			return Objects.equals(this.version, that.version);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(version());
		}

		@Override
		public int compareTo(ReleaseWrite o) {
			return -new ComparableVersion(this.version).compareTo(new ComparableVersion(o.version));
		}
	}

	enum Status {

		/**
		 * The status of a release version.
		 */
		SNAPSHOT, PRERELEASE, GENERAL_AVAILABILITY

	}

}
