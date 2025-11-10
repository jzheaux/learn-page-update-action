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
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.spring.github.actions.releasesapisync.releases.ReleasesService;
import io.spring.github.actions.releasesapisync.releases.ReleasesService.FetchedRelease;
import io.spring.github.actions.releasesapisync.releases.ReleasesService.Release;

@Component
class ReleaseUpdater {
    private final ReleasesService releases;

    private final String slug;
    private final Release latestVersion;
    
    ReleaseUpdater(ReleasesService releases, ReleasesApiSyncProperties properties) {
        this.releases = releases;
        this.latestVersion = properties.project().getRelease();
        this.slug = properties.project().slug();
    }

    void update() {
        Collection<FetchedRelease> existingReleases = this.releases.getReleases(this.slug);
        Collection<FetchedRelease> isSameMajorMinor = existingReleases.stream()
            .filter(this.latestVersion::isSameMajorMinor)
            .collect(Collectors.toList());
        Collection<Release> toCreate = List.of(this.latestVersion, this.latestVersion.nextSnapshot());
        for (FetchedRelease parts : isSameMajorMinor) {
            this.releases.deleteRelease(this.slug, parts.version());
        }
        for (Release parts : toCreate) {
            this.releases.createRelease(this.slug, parts);
        }
    }
}