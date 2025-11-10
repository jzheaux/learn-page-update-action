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

import io.spring.github.actions.releasesapisync.ReleasesApiSyncProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(properties = {
		"releases.api.url=http://localhost:8080",
		"releases.api.token=token",
		"releases.project.name=spring-boot",
		"releases.project.version=2.3.1"
}, value = ReleasesService.class)
@Import(ReleaseSyncConfiguration.class)
class ReleasesServiceTests {

	static final String API_BASE = "http://localhost:8080/projects/spring-boot";

	static final String RELEASE_TEMPLATE = """
			{
				"version" : "{version}",
				"apiDocUrl" : "https://docs.spring.io/spring-boot/docs/{version}/api/",
				"referenceDocUrl" : "https://docs.spring.io/spring-boot/docs/{version}/reference/html/",
				"status" : "{status}",
				"current" : true,
				"_links" : {
					"repository" : {
						"href" : "https://api.spring.io/repositories/spring-releases"
					},
					"self" : {
						"href" : "https://api.spring.io/projects/spring-boot/releases/{version}"
					}
				}
			}
			""";

	static final String RELEASES_TEMPLATE = """
			{
				"_embedded" : {
					"releases" : {releases}
				},
				"_links" : {
					"project" : {
						"href" : "https://api.spring.io/projects/spring-boot"
					},
					"current" : {
						"href" : "https://api.spring.io/projects/spring-boot/releases/current"
					}
				}
			}
			""";

	@Autowired
	MockRestServiceServer server;

	@Autowired
	ReleasesService releases;

	@Autowired
	ReleasesApiSyncProperties properties;

	@Test
	void updateWhenNoReleasesThenAdds() {
		get().andRespond(withSuccess(releases(), MediaType.APPLICATION_JSON));
		post().andExpect(content().string(containsString("2.3.1"))).andRespond(withSuccess());
		post().andExpect(content().string(containsString("2.3.2-SNAPSHOT"))).andRespond(withSuccess());
		this.releases.syncReleases(this.properties.project().getRelease());
		this.server.verify();
	}

	@Test
	void updateWhenExistingReleasesThenReplaces() {
		String releases = releases(ga("2.3.0"), snapshot("2.3.1-SNAPSHOT"));
		get().andRespond(withSuccess(releases, MediaType.APPLICATION_JSON));
		delete("2.3.0").andRespond(withSuccess());
		delete("2.3.1-SNAPSHOT").andRespond(withSuccess());
		post().andExpect(content().string(containsString("2.3.1"))).andRespond(withSuccess());
		post().andExpect(content().string(containsString("2.3.2-SNAPSHOT"))).andRespond(withSuccess());
		this.releases.syncReleases(this.properties.project().getRelease());
	}

	@Test
	void updateWhenMultipleExistingReleasesThenReplaces() {
		String releases = releases(ga("2.3.0"), snapshot("2.3.1-SNAPSHOT"),
				ga("2.3.1"), snapshot("2.3.2-SNAPSHOT"));
		get().andRespond(withSuccess(releases, MediaType.APPLICATION_JSON));
		delete("2.3.0").andRespond(withSuccess());
		delete("2.3.1-SNAPSHOT").andRespond(withSuccess());
		delete("2.3.1").andRespond(withSuccess());
		delete("2.3.2-SNAPSHOT").andRespond(withSuccess());
		post().andExpect(content().string(containsString("2.3.1"))).andRespond(withSuccess());
		post().andExpect(content().string(containsString("2.3.2-SNAPSHOT"))).andRespond(withSuccess());
		this.releases.syncReleases(this.properties.project().getRelease());
	}

	ResponseActions get() {
		return this.server.expect(requestTo(API_BASE + "/releases")).andExpect(method(HttpMethod.GET));
	}

	ResponseActions post() {
		return this.server.expect(requestTo(API_BASE + "/releases")).andExpect(method(HttpMethod.POST));
	}

	ResponseActions delete(String version) {
		return this.server.expect(requestTo(API_BASE + "/releases/" + version))
				.andExpect(method(HttpMethod.DELETE));
	}

	String ga(String version) {
		return RELEASE_TEMPLATE.replaceAll("\\{version}", version)
				.replace("{status}", "GENERAL_AVAILABILITY");
	}

	String snapshot(String version) {
		return RELEASE_TEMPLATE.replaceAll("\\{version}", version)
				.replace("{status}", "SNAPSHOT");
	}

	String releases(String... releases) {
		return RELEASES_TEMPLATE.replace("{releases}", List.of(releases).toString());
	}

}