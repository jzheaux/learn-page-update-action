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

import io.spring.github.actions.releasesapisync.releases.ReleasesService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties(ReleasesApiSyncProperties.class)
public class ReleaseApiSyncApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext app = SpringApplication.run(ReleaseApiSyncApplication.class, args);
		ReleasesApiSyncProperties properties = app.getBean(ReleasesApiSyncProperties.class);
		app.getBean(ReleasesService.class).syncReleases(properties.project().getRelease());
	}

}
