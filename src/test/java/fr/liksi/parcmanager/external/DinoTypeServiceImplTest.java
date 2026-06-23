package fr.liksi.parcmanager.external;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class DinoTypeServiceImplTest {

    @Autowired
    private DinoTypeService dinoTypeService;

    @MockitoBean
    private DinoTypeApiClient dinoTypeApiClient;

    @Test
    void shouldReturnDinoTypeWhenDinoExists() {
        // Given
        final var dinoId = UUID.randomUUID();
        final var expectedDto = new DinoTypeDto(dinoId, "TREX", "Theropoda");

        when(dinoTypeApiClient.getDinoType(dinoId)).thenReturn(expectedDto);

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
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        assertThatThrownBy(() -> dinoTypeService.getDinoType(dinoId))
            .isInstanceOf(HttpClientErrorException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(dinoTypeApiClient).getDinoType(dinoId);
    }
}
