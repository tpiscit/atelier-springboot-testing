package fr.liksi.parcmanager.it.testconf;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Configuration IT — MockWebServer de l'API REST externe {@code dinotype}.
 *
 * <p>Démarre un {@link MockWebServer} sur un port aléatoire et stubbe l'endpoint
 * {@code GET /api/dinotype?id=...} utilisé dans les scénarios IT.
 *
 * <p>Le {@link MockWebServer} est exposé comme bean Spring avec
 * {@code destroyMethod = "shutdown"} : Spring le ferme à la destruction du contexte
 * de test, évitant les fuites de threads/ports entre suites IT. L'URL de base est
 * publiée dans {@code dinotype.api.url} via un {@link DynamicPropertyRegistrar}.
 *
 * <p>Importée explicitement via {@code @Import} dans les tests IT.
 */
@TestConfiguration
public class DinoTypeApiItConfiguration {

    /**
     * Construit l'URL de base du {@link MockWebServer} (sans slash final).
     *
     * @param server serveur dont on extrait l'URL
     * @return URL HTTP au format {@code http://host:port}
     */
    public static String dinoTypeEndpoint(MockWebServer server) {
        return server.url("/").toString().replaceAll("/$", "");
    }

    /**
     * MockWebServer dinotype démarré au câblage du bean. Spring le ferme via
     * {@code shutdown()} à la destruction du contexte.
     */
    @Bean(destroyMethod = "shutdown")
    MockWebServer dinoTypeMockWebServer() {
        final var server = new MockWebServer();
        server.setDispatcher(new DinoTypeItDispatcher());
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de démarrer le MockWebServer dinotype IT", e);
        }
        return server;
    }

    @Bean
    DynamicPropertyRegistrar dinoTypeItDynamicProperties(MockWebServer dinoTypeMockWebServer) {
        return registry -> registry.add("dinotype.api.url", () -> dinoTypeEndpoint(dinoTypeMockWebServer));
    }

    private static final class DinoTypeItDispatcher extends Dispatcher {

        @Override
        public @NonNull MockResponse dispatch(RecordedRequest request) {
            final var url = request.getRequestUrl();
            if ("GET".equals(request.getMethod()) && url != null && "/api/dinotype".equals(url.encodedPath())) {
                final var body = """
                        {"guid":"%s","species":"TREX","family":"Theropoda"}
                        """.formatted(url.queryParameter("id"));
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(body);
            }
            return new MockResponse().setResponseCode(404);
        }
    }
}
