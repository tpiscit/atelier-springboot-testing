package fr.liksi.parcmanager.it;

import fr.liksi.parcmanager.it.testconf.DinoTypeApiItConfiguration;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'integration boite noire de l'affectation d'un dino.
 *
 * <p>Le test n'interagit avec l'application que via HTTP (client JDK) et compare
 * la reponse JSON au resultat attendu.
 */
@ActiveProfiles("it")
@Import(DinoTypeApiItConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DinoAssignmentIT {

    /** Identifiant fixe du dino a affecter (repris dans le JSON attendu). */
    private static final String DINO_ID = "550e8400-e29b-41d4-a716-446655440000";

    @LocalServerPort
    int port;

    @Test
    void affecteUnTrexAuPremierEnclosDisponibleDeHawaii() throws Exception {
        // Given : l'API dinotype renvoie l'espece TREX pour le dino demande
        // When : appel HTTP boite noire sur l'endpoint d'affectation
        final var httpResponse = sendAssignRequest();

        // Then : reponse 200 + parc HAWAII avec le TREX place dans l'enclos 1
        assertThat(httpResponse.statusCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedBody(), httpResponse.body(), JSONCompareMode.LENIENT);
    }

    private HttpResponse<String> sendAssignRequest() throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/dinos/assign"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("\"" + DinoAssignmentIT.DINO_ID + "\""))
            .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String expectedBody() {
        try (var is = DinoAssignmentIT.class.getClassLoader()
            .getResourceAsStream("expected/assign-trex-hawaii.json")) {
            final var template = new String(
                Objects.requireNonNull(is, "JSON attendu introuvable").readAllBytes(),
                StandardCharsets.UTF_8);
            return template.replace("__DINO_ID__", DINO_ID);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
