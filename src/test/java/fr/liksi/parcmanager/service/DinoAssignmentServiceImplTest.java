package fr.liksi.parcmanager.service;

import fr.liksi.parcmanager.model.entity.*;
import fr.liksi.parcmanager.model.enums.*;
import fr.liksi.parcmanager.repository.ParcRepository;
import fr.liksi.parcmanager.service.dinospecies.DinoSpeciesService;
import fr.liksi.parcmanager.service.exception.DinoAlreadyExistsException;
import fr.liksi.parcmanager.service.exception.NoSuitableEnclosException;
import fr.liksi.parcmanager.service.exception.SpeciesNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class DinoAssignmentServiceImplTest {

    @Autowired
    private DinoAssignmentService dinoAssignmentService;

    @MockitoBean
    private ParcRepository parcRepository;

    @MockitoBean
    private DinoSpeciesService dinoSpeciesService;

    private final DinoSpecies trexSpecies = new DinoSpecies(
            "TREX",
            TypeNourriture.ANIMAL,
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(50),
            List.of(Typologie.PLAINE, Typologie.FORET),
            BigDecimal.valueOf(200),
            List.of(Climat.CHAUD, Climat.TEMPERE)
    );

    private final DinoSpecies velociraptorSpecies = new DinoSpecies(
            "VELOCIRAPTOR",
            TypeNourriture.ANIMAL,
            BigDecimal.valueOf(30),
            BigDecimal.valueOf(20),
            List.of(Typologie.FORET),
            BigDecimal.valueOf(50),
            List.of(Climat.CHAUD)
    );
    @BeforeEach
    void setUp() {
    }

    @Test
    void unTrexEstAffecteAUnEnclosPlaineAdapte() {
        // Given
        final var trexId = unTrexAAffecter();
        final var parc = parcHawaiiOuvert();
        final var enclos = ajouterUnEnclos(parc, Typologie.PLAINE, 1000, 500, 200);
        ajouterNouveauxParcs(parc);

        // When
        final var response = dinoAssignmentService.assignDino(trexId);

        // Then
        assertThat(response.parcName()).isEqualTo(NomParc.HAWAII);
        assertThat(enclos.getDinos())
            .singleElement()
            .satisfies(dino -> {
                assertThat(dino.getId()).isEqualTo(trexId);
                assertThat(dino.getSpecies()).isEqualTo("TREX");
            });
        verify(parcRepository).saveDino(trexId, "TREX", enclos.getId());
    }

    @Test
    void unDinoDEspeceInconnueEstRefuse() {
        // Given
        final var dinoId = unDinoDEspeceInconnue();

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(dinoId))
            .isInstanceOf(SpeciesNotFoundException.class);
        verify(parcRepository, never()).findCandidateParcs(any());
    }

    @Test
    void unDinoDejaAffecteEstRefuse() {
        // Given
        final var dinoId = unDinoDejaAffecte();

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(dinoId))
            .isInstanceOf(DinoAlreadyExistsException.class)
            .hasMessageContaining(dinoId.toString());
        verify(dinoSpeciesService, never()).findDinoSpeciesByDinoId(any());
        verify(parcRepository, never()).findCandidateParcs(any());
    }

    @Test
    void lAffectationEchoueQuandAucunParcNeCorrespondAuClimat() {
        // Given
        final var trexId = unTrexAAffecter();
        when(parcRepository.findCandidateParcs(any())).thenReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class)
            .hasMessageContaining("TREX");
    }

    @Test
    void unEnclosALaTypologieIncompatibleEstRefuse() {
        // Given
        final var trexId = unTrexAAffecter();
        final var parc = parcHawaiiOuvert();
        ajouterUnEnclos(parc, Typologie.EAU, 1000, 500, 200);
        ajouterNouveauxParcs(parc);

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    @Test
    void unEnclosTropPetitEstRefuse() {
        // Given
        final var trexId = unTrexAAffecter();
        final var parc = parcHawaiiOuvert();
        ajouterUnEnclos(parc, Typologie.PLAINE, 100, 500, 200);
        ajouterNouveauxParcs(parc);

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    @Test
    void unEnclosSansAssezDEauEstRefuse() {
        // Given
        final var trexId = unTrexAAffecter();
        final var parc = parcHawaiiOuvert();
        ajouterUnEnclos(parc, Typologie.PLAINE, 1000, 10, 200);
        ajouterNouveauxParcs(parc);

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    @Test
    void unEnclosSansAssezDeNourritureEstRefuse() {
        // Given
        final var trexId = unTrexAAffecter();
        final var parc = parcHawaiiOuvert();
        ajouterUnEnclos(parc, Typologie.PLAINE, 1000, 500, 50);
        ajouterNouveauxParcs(parc);

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    @Test
    void unEnclosAvecLeMauvaisTypeDeNourritureEstRefuse() {
        // Given
        final var trexId = unTrexAAffecter();
        final var parc = parcHawaiiOuvert();
        final var enclos = new Enclos(Typologie.PLAINE, BigDecimal.valueOf(1000));
        enclos.setId(1L);
        parc.addEnclos(enclos);
        enclos.addRessource(new Eau(TypeEau.DOUCE, BigDecimal.valueOf(500)));
        enclos.addRessource(new Nourriture(TypeNourriture.VEGETAL, BigDecimal.valueOf(200)));
        ajouterNouveauxParcs(parc);

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UUID unTrexAAffecter() {
        final var dinoId = UUID.randomUUID();
        when(dinoSpeciesService.findDinoSpeciesByDinoId(dinoId))
            .thenReturn(Optional.of(trexSpecies));
        return dinoId;
    }

    private UUID unDinoDEspeceInconnue() {
        final var dinoId = UUID.randomUUID();
        when(dinoSpeciesService.findDinoSpeciesByDinoId(dinoId)).thenReturn(Optional.empty());
        return dinoId;
    }

    private UUID unDinoDejaAffecte() {
        final var dinoId = UUID.randomUUID();
        when(parcRepository.existsDinoById(dinoId)).thenReturn(true);
        return dinoId;
    }

    private Parc parcHawaiiOuvert() {
        final var parc = new Parc(NomParc.HAWAII, Climat.CHAUD, StatutParc.OUVERT);
        parc.setId(1L);
        return parc;
    }

    private Enclos ajouterUnEnclos(Parc parc, Typologie typologie,
                                        int surface, int eau, int viande) {
        final var enclos = new Enclos(typologie, BigDecimal.valueOf(surface));
        enclos.setId(1L);
        parc.addEnclos(enclos);
        enclos.addRessource(new Eau(TypeEau.DOUCE, BigDecimal.valueOf(eau)));
        enclos.addRessource(new Nourriture(TypeNourriture.ANIMAL, BigDecimal.valueOf(viande)));
        return enclos;
    }

    private void ajouterNouveauxParcs(Parc... parcs) {
        when(parcRepository.findCandidateParcs(any())).thenReturn(List.of(parcs));
    }
}
