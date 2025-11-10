package io.spring.github.actions.releasesapisync.releases;

import java.util.Collection;

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
