package fr.liksi.parcmanager.external;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;
import fr.liksi.parcmanager.external.Fake.DinoTypeMockWebServerDispatcher;
import fr.liksi.parcmanager.external.Fake.DinoTypeFake;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DinoTypeServiceImplTest {

    private MockWebServer mockWebServer;
    private DinoTypeFake stub;
    private DinoTypeService dinoTypeService;

    @BeforeEach
    void startMockWebServer() throws IOException {
        stub = new DinoTypeFake();
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new DinoTypeMockWebServerDispatcher(stub));
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
        stub.add(new DinoTypeDto(dinoId, "TREX", "Theropoda"));

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
        // Given : aucun dino enregistré dans le Fake -> l'API répond 404

        // When
        final var result = dinoTypeService.getDinoType(UUID.randomUUID());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldPropagateServerErrors() {
        // Given
        stub.simulateServerError();

        // When & Then
        assertThatThrownBy(() -> dinoTypeService.getDinoType(UUID.randomUUID()))
            .isInstanceOf(HttpServerErrorException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private DinoTypeApiClient buildDinoTypeApiClientFromMockWebServer(MockWebServer server) {
        final var adapter = RestClientAdapter.create(RestClient.create(server.url("/").toString()));
        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(DinoTypeApiClient.class);
    }
}
