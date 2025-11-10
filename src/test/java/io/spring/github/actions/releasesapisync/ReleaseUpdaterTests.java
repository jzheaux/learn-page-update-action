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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.spring.github.actions.releasesapisync.releases.ReleasesService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.spring.github.actions.releasesapisync.ReleasesApiSyncProperties.Api;
import io.spring.github.actions.releasesapisync.ReleasesApiSyncProperties.Apidoc;
import io.spring.github.actions.releasesapisync.ReleasesApiSyncProperties.Project;
import io.spring.github.actions.releasesapisync.ReleasesApiSyncProperties.Refdoc;
import io.spring.github.actions.releasesapisync.releases.ReleasesService.FetchedRelease;
import io.spring.github.actions.releasesapisync.releases.ReleasesService.Release;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class ReleaseUpdaterTests {

    ReleasesApiSyncProperties properties = new ReleasesApiSyncProperties(
        new Api("url", "token"), 
        new Project("spring-security", "6.5.4", new Apidoc("template"), new Refdoc("template", true))
    );

    @Test
    void updateWhenNoReleasesThenAdds() {
        ReleasesService releases = mock(ReleasesService.class);
        given(releases.getReleases(any())).willReturn(Collections.emptyList());
        ReleaseUpdater updater = new ReleaseUpdater(releases, properties);
        updater.update();
        ArgumentCaptor<Release> captor = ArgumentCaptor.forClass(Release.class);
        verify(releases, times(2)).createRelease(any(), captor.capture());
        Collection<Release> created = captor.getAllValues();
        assertThat(created).hasSize(2);
    }

    @Test
    void updateWhenExistingReleasesThenReplaces() {
        ReleasesService releases = mock(ReleasesService.class);
        FetchedRelease release = new FetchedRelease("6.5.4", "refdocUrl", "apidocUrl", "GA", true);
        FetchedRelease snapshot = new FetchedRelease("6.5.5-SNAPSHOT", "refdocUrl", "apidocUrl", "SNAPSHOT", false);
        given(releases.getReleases(any())).willReturn(List.of(release, snapshot));
        ReleaseUpdater updater = new ReleaseUpdater(releases, properties);
        updater.update();
        ArgumentCaptor<String> deletedCaptor = ArgumentCaptor.forClass(String.class);
        verify(releases, times(2)).deleteRelease(any(), deletedCaptor.capture());
        assertThat(deletedCaptor.getAllValues()).contains("6.5.4", "6.5.5-SNAPSHOT");
        ArgumentCaptor<Release> createdCaptor = ArgumentCaptor.forClass(Release.class);
        verify(releases, times(2)).createRelease(any(), createdCaptor.capture());
        Collection<Release> created = createdCaptor.getAllValues();
        assertThat(created).hasSize(2);
    }

}