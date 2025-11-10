package io.spring.github.actions.releasesapisync.releases;

import java.util.Collection;

class ReleasesServiceAdapter implements ReleasesService {
	private final EmbeddedReleasesService embedded;

	ReleasesServiceAdapter(EmbeddedReleasesService embedded) {
		this.embedded = embedded;
	}

	@Override
	public Collection<FetchedRelease> getReleases(String project) {
		return this.embedded.getEmbeddedReleases(project).releases();
	}

	@Override
	public void createRelease(String project, Release release) {
		this.embedded.createRelease(project, release);
	}

	@Override
	public void deleteRelease(String project, String release) {
		this.embedded.deleteRelease(project, release);
	}
}
