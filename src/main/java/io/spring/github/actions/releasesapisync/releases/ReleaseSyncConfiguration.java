package io.spring.github.actions.releasesapisync.releases;

import io.spring.github.actions.releasesapisync.ReleasesApiSyncProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class ReleaseSyncConfiguration {
	@Bean
	ReleasesService releasesService(RestClient.Builder builder, ReleasesApiSyncProperties properties) {
		addBase(builder, properties);
		addAuthentication(builder, properties);
		var factory = HttpServiceProxyFactory
				.builderFor(RestClientAdapter.create(builder.build()))
				.build();
		var releases = factory.createClient(EmbeddedReleasesService.class);
		return new ReleasesServiceAdapter(properties.project().name(), releases);

	}

	private void addBase(RestClient.Builder builder, ReleasesApiSyncProperties properties) {
		var url = properties.api().url();
		builder.baseUrl(url);
	}

	private void addAuthentication(RestClient.Builder builder, ReleasesApiSyncProperties properties) {
		var name = properties.project().name();
		var token = properties.api().token();
		builder.requestInterceptor(new BasicAuthenticationInterceptor(name, token));
	}
}
