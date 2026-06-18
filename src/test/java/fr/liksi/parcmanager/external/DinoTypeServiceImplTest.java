package fr.liksi.parcmanager.external;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DinoTypeServiceImplTest {

    private static final String RESPONSE_TEMPLATE = loadTemplate();

    private MockWebServer mockWebServer;

    private DinoTypeService dinoTypeService;

    @BeforeEach
    void startMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        dinoTypeService = new DinoTypeServiceImpl(buildDinoTypeApiClientFromMockWebServer(mockWebServer));
    }

    @AfterEach
    void stopMockWebServer() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnDinoTypeWhenDinoExists() {
        // Given
        final var dinoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setHeader("Content-Type", "application/json")
                .setBody(RESPONSE_TEMPLATE.formatted(dinoId, "TREX", "Theropoda")));

        // When
        final var result = dinoTypeService.getDinoType(dinoId);

        // Then
        assertThat(result).hasValueSatisfying(dto -> {
            assertThat(dto.guid()).isEqualTo(dinoId);
            assertThat(dto.species()).isEqualTo("TREX");
            assertThat(dto.family()).isEqualTo("Theropoda");
        });
    }

    @Test
    void shouldReturnEmptyWhen404() {
        // Given
        final var dinoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));


        // When
        final var result = dinoTypeService.getDinoType(dinoId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldPropagateOtherHttpClientErrorExceptions() {
        // Given
        final var dinoId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR));

        // When & Then
        assertThatThrownBy(() -> dinoTypeService.getDinoType(dinoId))
                .isInstanceOf(HttpServerErrorException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private DinoTypeApiClient buildDinoTypeApiClientFromMockWebServer(MockWebServer server) {
        final var adapter = RestClientAdapter.create(RestClient.create(server.url("/").toString()));
        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(DinoTypeApiClient.class);
    }

    private static String loadTemplate() {
        try (var is = DinoTypeServiceImplTest.class.getClassLoader()
                .getResourceAsStream("dinotype/dinotype-response-template.json")) {
            return new String(Objects.requireNonNull(is, "template JSON introuvable").readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
