package io.spring.github.actions.releasesapisync.releases;

import java.util.Collection;

import io.spring.github.actions.releasesapisync.releases.ReleasesService.FetchedRelease;
import io.spring.github.actions.releasesapisync.releases.ReleasesService.Release;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

interface EmbeddedReleasesService {

	@GetExchange("/projects/{project}/releases")
	EmbeddedReleases getEmbeddedReleases(@PathVariable(name="project") String project);

	@PostExchange("/projects/{project}/releases")
	void createRelease(@PathVariable(name="project") String project, @RequestBody Release release);

	@DeleteExchange("/projects/{project}/releases/{release}")
	void deleteRelease(@PathVariable(name="project") String project, @PathVariable(name="release") String release);

	record EmbeddedReleases(Collection<FetchedRelease> releases) {
	}
}
