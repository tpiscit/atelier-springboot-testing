package fr.liksi.parcmanager.config;

import fr.liksi.parcmanager.external.DinoTypeApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient dinoTypeRestClient(@Value("${dinotype.api.url}") String baseUrl) {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(RestClient dinoTypeRestClient) {
        return HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(dinoTypeRestClient))
            .build();
    }

    @Bean
    public DinoTypeApiClient dinoTypeClient(HttpServiceProxyFactory factory) {
        return factory.createClient(DinoTypeApiClient.class);
    }
}
