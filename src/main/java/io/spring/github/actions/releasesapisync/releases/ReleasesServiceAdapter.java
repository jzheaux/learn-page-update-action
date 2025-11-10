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

/**
 * An adapter that includes the project slug and also unwraps any embedded wrapper in the
 * JSON response.
 *
 * @author Josh Cummings
 */
class ReleasesServiceAdapter implements ReleasesService {

	private final String slug;

	private final EmbeddedReleasesService embedded;

	ReleasesServiceAdapter(String slug, EmbeddedReleasesService embedded) {
		this.slug = slug;
		this.embedded = embedded;
	}

	@Override
	public Collection<FetchedRelease> getReleases() {
		return this.embedded.getEmbeddedReleases(this.slug)._embedded().releases();
	}

	@Override
	public void createRelease(Release release) {
		this.embedded.createRelease(this.slug, release);
	}

	@Override
	public void deleteRelease(String release) {
		this.embedded.deleteRelease(this.slug, release);
	}

}
