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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import io.spring.github.actions.releasesapisync.releases.ReleasesService.Release;

/**
 * Configuration properties for the releases API sync action.
 */
@ConfigurationProperties(prefix = "releases")
public record ReleasesApiSyncProperties(@DefaultValue Api api, @DefaultValue Project project) {

    public record Api(@DefaultValue("https://api.spring.io") String url, String token) {
    }

    public record Project(String slug, String version, @DefaultValue Apidoc apidoc, @DefaultValue Refdoc refdoc) {
        public Release getRelease() {
            String refdocUrl = this.refdoc.template().replace("{slug}", this.slug);
            String apidocUrl = this.apidoc.template().replace("{slug}", this.slug);
            return new Release(this.version, this.refdoc.antora, refdocUrl, apidocUrl);
        }
    }

    record Apidoc(@DefaultValue("https://docs.spring.io/{slug}/docs/{version}/javadoc-api") String template) {
    }

    record Refdoc(@DefaultValue("https://docs.spring.io/{slug}/reference/{version}/index.html") String template, @DefaultValue("true") boolean antora) {
    }
}