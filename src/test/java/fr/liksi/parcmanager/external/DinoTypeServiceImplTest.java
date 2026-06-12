package fr.liksi.parcmanager.external;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class DinoTypeServiceImplTest {

    private static final String RESPONSE_TEMPLATE = loadTemplate();

    private MockWebServer mockWebServer;

    @Autowired
    private DinoTypeService dinoTypeService;

    //TODO A supprimer : inutile vu qu'on ne mock plus à ce niveau
    @MockitoBean
    private DinoTypeApiClient dinoTypeApiClient;

    @BeforeEach
    void startMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        //TODO instancier DinoTypeService
        buildDinoTypeApiClientFromMockWebServer(mockWebServer);
    }

    @AfterEach
    void stopMockWebServer() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnDinoTypeWhenDinoExists() {
        // Given
        final var dinoId = UUID.randomUUID();
        final var expectedDto = new DinoTypeDto(dinoId, "TREX", "Theropoda");

        when(dinoTypeApiClient.getDinoType(dinoId)).thenReturn(expectedDto);
        /*TODO
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setHeader("Content-Type", "application/json")
                .setBody(TODO);*/

        // When
        final var result = dinoTypeService.getDinoType(dinoId);

        // Then
        assertThat(result).hasValueSatisfying(dto -> {
            assertThat(dto.guid()).isEqualTo(dinoId);
            assertThat(dto.species()).isEqualTo("TREX");
            assertThat(dto.family()).isEqualTo("Theropoda");
        });

        verify(dinoTypeApiClient).getDinoType(dinoId);
    }

    @Test
    void shouldReturnEmptyWhen404() {
        // Given
        final var dinoId = UUID.randomUUID();

        when(dinoTypeApiClient.getDinoType(dinoId))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        /*TODO use HttpURLConnection
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(TODO));*/


        // When
        final var result = dinoTypeService.getDinoType(dinoId);

        // Then
        assertThat(result).isEmpty();

        verify(dinoTypeApiClient).getDinoType(dinoId);
    }

    @Test
    void shouldPropagateOtherHttpClientErrorExceptions() {
        // Given
        final var dinoId = UUID.randomUUID();

        when(dinoTypeApiClient.getDinoType(dinoId))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        /*TODO use HttpURLConnection
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(TODO));*/

        // When & Then
        assertThatThrownBy(() -> dinoTypeService.getDinoType(dinoId))
            .isInstanceOf(HttpServerErrorException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(dinoTypeApiClient).getDinoType(dinoId);
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
