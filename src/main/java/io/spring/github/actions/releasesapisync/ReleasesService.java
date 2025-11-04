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

package io.spring.github.actions.releasesapisync;

import java.util.Collection;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

interface ReleasesService {
    @GetExchange("/projects/{project}/releases")
    Collection<FetchedRelease> getReleases(@PathVariable(name="project") String project);

    @PostExchange("/projects/{project}/releases")
    void createRelease(@PathVariable(name="project") String project, @RequestBody Release release);

    @DeleteExchange("/projects/{project}/releases/{release}")
    void deleteRelease(@PathVariable(name="project") String project, @PathVariable(name="release") String release);

    record FetchedRelease(String version, String referenceDocUrl, String apiDocUrl, String status, boolean current) {
    }

    record Release(String version, boolean isAntora, String referenceDocUrl, String apiDocUrl) {
        boolean isSameMajorMinor(FetchedRelease other) {
            String[] parts = this.version.split("[\\.\\-]");
            String[] otherParts = other.version().split("[\\.\\-]");
            return parts[0].equals(otherParts[0]) && parts[1].equals(otherParts[1]);
        }

        Release nextSnapshot() {
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