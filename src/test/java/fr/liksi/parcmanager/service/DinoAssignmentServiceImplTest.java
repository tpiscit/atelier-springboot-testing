package fr.liksi.parcmanager.service;

import fr.liksi.parcmanager.external.DinoTypeApiClient;
import fr.liksi.parcmanager.external.DinoTypeServiceImpl;
import fr.liksi.parcmanager.external.dto.DinoTypeDto;
import fr.liksi.parcmanager.model.entity.*;
import fr.liksi.parcmanager.model.enums.*;
import fr.liksi.parcmanager.repository.ParcRepository;
import fr.liksi.parcmanager.service.dinospecies.DinoSpeciesRegistry;
import fr.liksi.parcmanager.service.dinospecies.DinoSpeciesServiceImpl;
import fr.liksi.parcmanager.service.exception.DinoAlreadyExistsException;
import fr.liksi.parcmanager.service.exception.NoSuitableEnclosException;
import fr.liksi.parcmanager.service.exception.SpeciesNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DinoAssignmentServiceImplTest {

    private DinoAssignmentService dinoAssignmentService;

    @Mock
    private ParcRepository parcRepository;

    @Mock
    private DinoTypeApiClient dinoTypeApiClient;

    @BeforeEach
    void setUp() {
        dinoAssignmentService = new DinoAssignmentServiceImpl(
                parcRepository,
                new DinoSpeciesServiceImpl(new DinoSpeciesRegistry(),new DinoTypeServiceImpl(dinoTypeApiClient)));
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
        verify(dinoTypeApiClient, never()).getDinoType(any());
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

    @Test
    void reproduceBug1042_trexShouldBeRejectedWhenVelociraptorAlreadyOccupiesEnclos() {
        // Given : l'enclos du ticket — 220 m², un Velociraptor (50 m²) déjà présent
        // (espèce stockée en base avec son nom d'affichage, comme constaté en prod)
        final var trexId = unTrexAAffecter();
        final var parc = parcHawaiiOuvert();
        final var enclos = ajouterUnEnclos(parc, Typologie.FORET, 220, 500, 300);
        final var velociraptor = new Dino(UUID.randomUUID(),"Velociraptor");
        enclos.addDino(velociraptor);
        ajouterNouveauxParcs(parc);

        // When & Then : l'affectation est bien refusée... le test passe, bug non reproduit !
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    @Test
    void unParcEnConstructionNAccueillePasDeDino() {
        final var trexId = unTrexAAffecter();
        // Given : aucun parc ouvert n'est disponible
        ajouterNouveauxParcs(creerParcAvecEnclosPourTrex(NomParc.NOIRMOUTIER, StatutParc.CONSTRUCTION));


        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Parc creerParcAvecEnclosPourTrex(NomParc nom, StatutParc statut) {
        final var parc = new Parc(nom, Climat.CHAUD, statut);
        parc.setId(1L);
        ajouterUnEnclos(parc, Typologie.PLAINE, 1000, 500, 200);
        return parc;
    }

    private UUID unTrexAAffecter() {
        final var dinoId = UUID.randomUUID();
        when(dinoTypeApiClient.getDinoType(dinoId))
            .thenReturn(new DinoTypeDto(dinoId, "TREX", "Theropoda"));
        return dinoId;
    }

    private UUID unDinoDEspeceInconnue() {
        final var dinoId = UUID.randomUUID();
        when(dinoTypeApiClient.getDinoType(dinoId)).thenReturn(null);
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
        /*TODO replace thenReturn with
        .thenAnswer(invocation -> {
            final StatutParc statutDemande = invocation.getArgument(1);
            return TODO
         */
    }
}
