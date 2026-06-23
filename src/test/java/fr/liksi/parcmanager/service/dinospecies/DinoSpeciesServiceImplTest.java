package fr.liksi.parcmanager.service.dinospecies;

import fr.liksi.parcmanager.external.DinoTypeService;
import fr.liksi.parcmanager.external.dto.DinoTypeDto;
import fr.liksi.parcmanager.model.entity.DinoSpecies;
import fr.liksi.parcmanager.model.enums.Climat;
import fr.liksi.parcmanager.model.enums.TypeNourriture;
import fr.liksi.parcmanager.model.enums.Typologie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DinoSpeciesServiceImplTest {

    @Mock
    private DinoSpeciesRegistry dinoSpeciesRegistry;

    @Mock
    private DinoTypeService dinoTypeService;

    @InjectMocks
    private DinoSpeciesServiceImpl dinoSpeciesService;

    @Test
    void shouldReturnSpeciesWhenDinoIdFound() {
        // Given
        final var dinoId = UUID.randomUUID();

        final var trex = new DinoSpecies(
            "TREX",
            TypeNourriture.ANIMAL,
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(50),
            List.of(Typologie.PLAINE, Typologie.FORET),
            BigDecimal.valueOf(200),
            List.of(Climat.CHAUD, Climat.TEMPERE)
        );

        when(dinoTypeService.getDinoType(dinoId))
            .thenReturn(Optional.of(new DinoTypeDto(dinoId, "TREX", "Theropoda")));
        when(dinoSpeciesRegistry.findBySpeciesName("TREX"))
            .thenReturn(Optional.of(trex));

        // When
        final var result = dinoSpeciesService.findDinoSpeciesByDinoId(dinoId);

        // Then
        assertThat(result).hasValueSatisfying(species -> {
            assertThat(species.getSpeciesName()).isEqualTo("TREX");
            assertThat(species.getFoodType()).isEqualTo(TypeNourriture.ANIMAL);
            assertThat(species.getFoodQuantity()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(species.getWaterQuantity()).isEqualTo(BigDecimal.valueOf(50));
            assertThat(species.getRequiredSurface()).isEqualTo(BigDecimal.valueOf(200));
            assertThat(species.getPreferredHabitats()).containsExactlyInAnyOrder(Typologie.PLAINE, Typologie.FORET);
            assertThat(species.getPreferredClimates()).containsExactlyInAnyOrder(Climat.CHAUD, Climat.TEMPERE);
        });

        verify(dinoTypeService).getDinoType(dinoId);
        verify(dinoSpeciesRegistry).findBySpeciesName("TREX");
    }

    @Test
    void shouldReturnEmptyWhenDinoIdNotFound() {
        // Given
        final var dinoId = UUID.randomUUID();

        when(dinoTypeService.getDinoType(dinoId))
            .thenReturn(Optional.empty());

        // When
        final var result = dinoSpeciesService.findDinoSpeciesByDinoId(dinoId);

        // Then
        assertThat(result).isEmpty();

        verify(dinoTypeService).getDinoType(dinoId);
    }

    @Test
    void shouldReturnEmptyWhenSpeciesNotFoundForDino() {
        // Given
        final var dinoId = UUID.randomUUID();

        when(dinoTypeService.getDinoType(dinoId))
            .thenReturn(Optional.of(new DinoTypeDto(dinoId, "UNKNOWN", null)));
        when(dinoSpeciesRegistry.findBySpeciesName("UNKNOWN"))
            .thenReturn(Optional.empty());

        // When
        final var result = dinoSpeciesService.findDinoSpeciesByDinoId(dinoId);

        // Then
        assertThat(result).isEmpty();

        verify(dinoTypeService).getDinoType(dinoId);
        verify(dinoSpeciesRegistry).findBySpeciesName("UNKNOWN");
    }
}
