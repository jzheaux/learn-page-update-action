package io.spring.github.actions.releasesapisync.releases;

import java.util.Base64;

import io.spring.github.actions.releasesapisync.ReleasesApiSyncProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@EnableConfigurationProperties(ReleasesApiSyncProperties.class)
@Configuration
class ReleaseSyncConfiguration {
	@Bean
	RestClient.Builder rest(ReleasesApiSyncProperties properties) {
		String authString = properties.project().slug() + ":" + properties.api().token();
		String base64Creds = Base64.getEncoder().encodeToString(authString.getBytes());
		return RestClient.builder()
				.baseUrl(properties.api().url())
				.defaultHeader("Authorization", "Basic " + base64Creds);
	}


	@Bean
	ReleasesService releasesService(RestClient.Builder rest) {
		RestClientAdapter adapter = RestClientAdapter.create(rest.build());
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return new ReleasesServiceAdapter(factory.createClient(EmbeddedReleasesService.class));
	}
}
