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

import java.util.Collection;

import io.spring.github.actions.releasesapisync.releases.ReleasesService.FetchedRelease;
import io.spring.github.actions.releasesapisync.releases.ReleasesService.Release;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Client interface for the Project Service API releases resource that includes the
 * embedded wrapper.
 *
 * @author Josh Cummings
 */
interface EmbeddedReleasesService {

	@GetExchange("/projects/{project}/releases")
	EmbeddedReleasesWrapper getEmbeddedReleases(@PathVariable(name = "project") String project);

	@PostExchange("/projects/{project}/releases")
	void createRelease(@PathVariable(name = "project") String project, @RequestBody Release release);

	@DeleteExchange("/projects/{project}/releases/{release}")
	void deleteRelease(@PathVariable(name = "project") String project, @PathVariable(name = "release") String release);

	record EmbeddedReleasesWrapper(EmbeddedReleases _embedded) {

	}

	record EmbeddedReleases(Collection<FetchedRelease> releases) {
	}

}
