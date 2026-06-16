package fr.liksi.parcmanager.external.Fake;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.UUID;

/**
 * Dispatcher okhttp MockWebServer pour les TU DinoTypeService.
 *
 * <p>Route les requêtes {@code GET /api/dinotype?id=...} en délégant la
 * résolution des réponses au {@link DinoTypeFake}. Le test n'interagit jamais
 * directement avec le MockWebServer : il alimente le Fake, et le dispatcher
 * lit le Fake à chaque requête entrante.
 *
 * <ul>
 *   <li>UUID trouvé dans le Fake → HTTP 200 + JSON (gabarit
 *       {@code dinotype/dinotype-response-template.json})</li>
 *   <li>UUID inconnu → HTTP 404</li>
 *   <li>Paramètre {@code id} absent ou UUID invalide → HTTP 400</li>
 *   <li>Mode erreur serveur actif → HTTP 500</li>
 *   <li>Tout autre chemin → HTTP 404</li>
 * </ul>
 */
public class DinoTypeMockWebServerDispatcher extends Dispatcher {

    private static final String RESPONSE_TEMPLATE = loadTemplate();

    private final DinoTypeFake stub;

    public DinoTypeMockWebServerDispatcher(DinoTypeFake stub) {
        this.stub = stub;
    }

    @Override
    public MockResponse dispatch(RecordedRequest request) {
        if (stub.isServerError()) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        final var url = Objects.requireNonNull(request.getRequestUrl());
        if (!"GET".equals(request.getMethod()) || !"/api/dinotype".equals(url.encodedPath())) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
        }

        final var idParam = url.queryParameter("id");
        if (idParam == null || idParam.isBlank()) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
        }

        final UUID id;
        try {
            id = UUID.fromString(idParam);
        } catch (IllegalArgumentException e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
        }

        return stub.findById(id)
            .map(DinoTypeMockWebServerDispatcher::toJsonResponse)
            .orElseGet(() -> new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));
    }

    private static MockResponse toJsonResponse(DinoTypeDto dto) {
        return new MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setHeader("Content-Type", "application/json")
            .setBody(RESPONSE_TEMPLATE.formatted(dto.guid(), dto.species(), dto.family()));
    }

    private static String loadTemplate() {
        try (var is = DinoTypeMockWebServerDispatcher.class.getClassLoader()
            .getResourceAsStream("dinotype/dinotype-response-template.json")) {
            return new String(Objects.requireNonNull(is, "template JSON introuvable").readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
