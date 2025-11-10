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

/**
 * Client interface for the Project Service API releases resource.
 *
 * @author Josh Cummings
 */
public interface ReleasesService {

	Collection<FetchedRelease> getReleases();

	void createRelease(Release release);

	void deleteRelease(String release);

	default void createReleases(Release... releases) {
		Arrays.stream(releases).forEach(this::createRelease);
	}

	default void deleteReleasesByGeneration(Release release) {
		getReleases().stream()
			.filter(release::isSameMajorMinor)
			.map(FetchedRelease::version)
			.forEach(this::deleteRelease);
	}

	default void syncReleases(Release latest) {
		deleteReleasesByGeneration(latest);
		createReleases(latest, latest.nextSnapshot());
	}

	record FetchedRelease(String version, String referenceDocUrl, String apiDocUrl, String status, boolean current) {
	}

	record Release(String version, boolean isAntora, String referenceDocUrl, String apiDocUrl) {

		public boolean isSameMajorMinor(FetchedRelease other) {
			String[] parts = this.version.split("[\\.\\-]");
			String[] otherParts = other.version().split("[\\.\\-]");
			return parts[0].equals(otherParts[0]) && parts[1].equals(otherParts[1]);
		}

		public Release nextSnapshot() {
			String[] parts = this.version.split("[\\.\\-]");
			int major = Integer.parseInt(parts[0]);
			int minor = Integer.parseInt(parts[1]);
			int patch = Integer.parseInt(parts[2]);
			boolean isSnapshot = this.version.endsWith("SNAPSHOT");
			if (!isSnapshot) {
				patch += 1;
			}
			String nextVersion = major + "." + minor + "." + patch + "-SNAPSHOT";
			return new Release(nextVersion, this.isAntora, this.referenceDocUrl, this.apiDocUrl);
		}
	}

}
