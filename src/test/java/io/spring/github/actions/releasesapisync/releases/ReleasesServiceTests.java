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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import io.spring.github.actions.releasesapisync.ReleasesApiSyncProperties;
import io.spring.github.actions.releasesapisync.releases.ReleasesService.ReleaseRead;
import io.spring.github.actions.releasesapisync.releases.ReleasesService.ReleaseWrite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@RestClientTest(properties = { "releases.api.token=token", "releases.project.slug=spring-boot",
		"releases.project.version=2.3.1" }, value = ReleasesService.class)
@Import(ReleaseSyncConfiguration.class)
class ReleasesServiceTests {

	@Autowired
	ReleasesService releases;

	@Autowired
	ReleasesApiSyncProperties properties;

	Path documentation = Path.of("spring-website-content/project/spring-boot", "documentation.json");

	@BeforeEach
	void createDocumentationFile() throws Exception {
		try {
			new File("spring-website-content/project/spring-boot").mkdirs();
			Files.writeString(this.documentation, "[]");
		}
		catch (Exception ex) {
			System.out.println("fail");
		}
	}

	@AfterEach
	void deleteDocumentationFile() throws Exception {
		Files.delete(this.documentation);
	}

	@Test
	void updateWhenNoReleasesThenAdds() throws Exception {
		this.releases.syncReleases(this.properties.project().getRelease());
		Collection<ReleaseRead> releases = this.releases.getReleases();
		assertThat(releases).extracting(ReleaseRead::version).containsExactly("2.3.2-SNAPSHOT", "2.3.1");
	}

	@Test
	void updateWhenExistingReleasesThenReplaces() {
		this.releases.createReleases(release("2.3.0"), release("2.3.1-SNAPSHOT"));
		this.releases.syncReleases(this.properties.project().getRelease());
		Collection<ReleaseRead> releases = this.releases.getReleases();
		assertThat(releases).extracting(ReleaseRead::version).containsExactly("2.3.2-SNAPSHOT", "2.3.1");
	}

	@Test
	void updateWhenMultipleExistingReleasesThenReplaces() {
		this.releases.createReleases(release("2.3.0"), release("2.3.1-SNAPSHOT"), release("2.3.1"),
				release("2.3.2-SNAPSHOT"));
		this.releases.syncReleases(this.properties.project().getRelease());
		Collection<ReleaseRead> releases = this.releases.getReleases();
		assertThat(releases).extracting(ReleaseRead::version).containsExactly("2.3.2-SNAPSHOT", "2.3.1");
	}

	ReleaseWrite release(String version) {
		return ReleaseWrite.fromVersion(version, true, "ref", "api");
	}

}
