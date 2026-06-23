package fr.liksi.parcmanager.service;

import fr.liksi.parcmanager.external.InMemoryDinoTypeService;
import fr.liksi.parcmanager.model.entity.*;
import fr.liksi.parcmanager.model.enums.*;
import fr.liksi.parcmanager.repository.InMemoryParcRepository;
import fr.liksi.parcmanager.service.dinospecies.DinoSpeciesRegistry;
import fr.liksi.parcmanager.service.dinospecies.DinoSpeciesServiceImpl;
import fr.liksi.parcmanager.service.exception.DinoAlreadyExistsException;
import fr.liksi.parcmanager.service.exception.NoSuitableEnclosException;
import fr.liksi.parcmanager.service.exception.SpeciesNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** TU de {@link DinoAssignmentService} : couche service réelle, seuls les ports d'I/O sont doublés. */
class DinoAssignmentServiceImplTest {

    private InMemoryParcRepository parcRepository;

    private InMemoryDinoTypeService dinoTypeService;

    private DinoAssignmentService dinoAssignmentService;

    @BeforeEach
    void setUp() {
        // Toute la couche service est réelle ; seuls les ports d'I/O sont doublés
        parcRepository = new InMemoryParcRepository();
        dinoTypeService = new InMemoryDinoTypeService();
        final var registry = new DinoSpeciesRegistry(
            new ClassPathResource("db/changelog/dino-species.json"));
        final var dinoSpeciesService = new DinoSpeciesServiceImpl(registry, dinoTypeService);
        dinoAssignmentService = new DinoAssignmentServiceImpl(parcRepository, dinoSpeciesService);
    }

    @Test
    void unTrexEstAffecteAUnEnclosPlaineAdapte() {
        // Given
        final var trexId = unDinoArrive("TREX");
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
        assertThat(parcRepository.existsDinoById(trexId)).isTrue();
    }

    @Test
    void unDinoDEspeceInconnueEstRefuse() {
        // Given
        final var dinoId = unDinoInconnu();

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(dinoId))
            .isInstanceOf(SpeciesNotFoundException.class);
    }

    @Test
    void unDinoDejaAffecteEstRefuse() {
        // Given
        final var dinoId = unDinoDejaAffecte();

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(dinoId))
            .isInstanceOf(DinoAlreadyExistsException.class)
            .hasMessageContaining(dinoId.toString());
    }

    @Test
    void lAffectationEchoueQuandAucunParcNeCorrespondAuClimat() {
        // Given
        final var trexId = unDinoArrive("TREX");

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class)
            .hasMessageContaining("TREX");
    }

    @Test
    void unEnclosALaTypologieIncompatibleEstRefuse() {
        // Given
        final var trexId = unDinoArrive("TREX");
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
        final var trexId = unDinoArrive("TREX");
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
        final var trexId = unDinoArrive("TREX");
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
        final var trexId = unDinoArrive("TREX");
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
        final var trexId = unDinoArrive("TREX");
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
    void unTrexRejointUnEnclosSpacieuxDejaOccupeParUnVelociraptor() {
        // Given
        final var trexId = unDinoArrive("TREX");
        final var parc = parcHawaiiOuvert();
        final var enclos = ajouterUnEnclos(parc, Typologie.FORET, 500, 500, 300);
        final var velociraptorId = unDinoArrive("VELOCIRAPTOR");
        ajouterNouveauxParcs(parc);
        dinoAssignmentService.assignDino(velociraptorId);

        // When
        final var response = dinoAssignmentService.assignDino(trexId);

        // Then
        assertThat(response.parcName()).isEqualTo(NomParc.HAWAII);
    }

    @Test
    void bug1042_unTrexEstRefuseQuandUnVelociraptorOccupeDejaUnEnclosTropPetit() {
        // Given
        final var trexId = unDinoArrive("TREX");
        final var parc = parcHawaiiOuvert();
        ajouterUnEnclos(parc, Typologie.FORET, 220, 500, 300);
        final var velociraptorId = unDinoArrive("VELOCIRAPTOR");
        ajouterNouveauxParcs(parc);
        dinoAssignmentService.assignDino(velociraptorId);

        // When & Then
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    @Test
    void reproduceBug1042_trexShouldBeRejectedWhenVelociraptorAlreadyOccupiesEnclos() {
        // Given : l'enclos du ticket — 220 m², un Velociraptor (50 m²) déjà présent
        // (espèce stockée en base avec son nom d'affichage, comme constaté en prod)
        final var parc = parcHawaiiOuvert();
        final var enclos = ajouterUnEnclos(parc, Typologie.FORET, 220, 500, 300);
        final var velociraptorId = unDinoArrive("VELOCIRAPTOR");
        ajouterNouveauxParcs(parc);
        dinoAssignmentService.assignDino(velociraptorId);

        // When & Then : il ne reste que 170 m², le T-Rex (200 m²) doit être refusé
        final var trexId = unDinoArrive("TREX");
        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    @Test
    void unParcEnConstructionNAccueillePasDeDino() {
        final var trexId = unDinoArrive("TREX");
        ajouterNouveauxParcs(creerParcAvecEnclosPourTrex(NomParc.NOIRMOUTIER, StatutParc.CONSTRUCTION));

        assertThatThrownBy(() -> dinoAssignmentService.assignDino(trexId))
            .isInstanceOf(NoSuitableEnclosException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UUID unDinoArrive(String species) {
        return dinoTypeService.givenDino(UUID.randomUUID(), species);
    }

    private UUID unDinoInconnu() {
        return UUID.randomUUID();
    }

    private UUID unDinoDejaAffecte() {
        final var dinoId = UUID.randomUUID();
        parcRepository.givenDinoDejaAffecte(dinoId);
        return dinoId;
    }

    private Parc parcHawaiiOuvert() {
        final var parc = new Parc(NomParc.HAWAII, Climat.CHAUD, StatutParc.OUVERT);
        parc.setId(1L);
        return parc;
    }

    private Parc creerParcAvecEnclosPourTrex(NomParc nom, StatutParc statut) {
        final var parc = new Parc(nom, Climat.CHAUD, statut);
        parc.setId(1L);
        ajouterUnEnclos(parc, Typologie.PLAINE, 1000, 500, 200);
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
        for (final var parc : parcs) {
            parcRepository.givenParc(parc);
        }
    }
}
