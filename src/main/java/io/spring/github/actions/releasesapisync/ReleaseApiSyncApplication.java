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

import java.util.Base64;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
@EnableConfigurationProperties(ReleasesApiSyncProperties.class)
class ReleaseApiSyncApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(ReleaseApiSyncApplication.class, args);
        app.getBean(ReleaseUpdater.class).update();
    }

    @Bean
    RestClient rest(ReleasesApiSyncProperties properties) {
        String authString = properties.project().slug() + ":" + properties.api().token();
        String base64Creds = Base64.getEncoder().encodeToString(authString.getBytes());
        return RestClient.builder()
            .baseUrl(properties.api().url())
            .defaultHeader("Authorization", "Basic " + base64Creds)
            .build();
    }

    @Bean
    ReleasesService releasesService(ReleasesApiSyncProperties properties, RestClient rest) {
        RestClientAdapter adapter = RestClientAdapter.create(rest);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ReleasesService.class);
    }
}