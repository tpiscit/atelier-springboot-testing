package fr.liksi.parcmanager.repository;

import fr.liksi.parcmanager.model.entity.Eau;
import fr.liksi.parcmanager.model.entity.Nourriture;
import fr.liksi.parcmanager.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/sql/cleanup-flyway-data.sql",
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@ActiveProfiles("test")
@JdbcTest
@Import(JdbcParcRepository.class)
class JdbcParcRepositoryTest {

    @Autowired
    private ParcRepository parcRepository;

    @Autowired
    private JdbcClient jdbcClient;

    private Long foretEnclosId;

    @BeforeEach
    void setUp() {
        final var parcKey = new GeneratedKeyHolder();
        jdbcClient.sql("INSERT INTO parc (nom, climat, statut) VALUES (?, ?, ?)")
            .params(NomParc.HAWAII.name(), Climat.CHAUD.name(), StatutParc.OUVERT.name())
            .update(parcKey, "id");
        final var hawaiiParcId = Objects.requireNonNull(parcKey.getKey()).longValue();

        final var enclosKey = new GeneratedKeyHolder();
        jdbcClient.sql("INSERT INTO enclos (typologie, surface, parc_id) VALUES (?, ?, ?)")
            .params(Typologie.FORET.name(), new BigDecimal("5000.00"), hawaiiParcId)
            .update(enclosKey, "id");
        foretEnclosId = Objects.requireNonNull(enclosKey.getKey()).longValue();

        jdbcClient.sql(
                "INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau) " +
                    "VALUES (?, ?, ?, ?, ?)")
            .params(Arrays.asList("NOURRITURE", new BigDecimal("500.00"), foretEnclosId, TypeNourriture.VEGETAL.name(), null))
            .update();

        jdbcClient.sql(
                "INSERT INTO ressource (type_ressource, quantite, enclos_id, type_nourriture, type_eau) " +
                    "VALUES (?, ?, ?, ?, ?)")
            .params(Arrays.asList("EAU", new BigDecimal("1000.00"), foretEnclosId, null, TypeEau.DOUCE.name()))
            .update();
    }

    @Test
    void shouldFindCandidateParcsByClimat() {
        final var results = parcRepository.findCandidateParcs(List.of(Climat.CHAUD), StatutParc.OUVERT);

        assertThat(results).hasSize(1);
        final var parc = results.getFirst();
        assertThat(parc.getNom()).isEqualTo(NomParc.HAWAII);
        assertThat(parc.getClimat()).isEqualTo(Climat.CHAUD);
        assertThat(parc.getStatut()).isEqualTo(StatutParc.OUVERT);
        assertThat(parc.getEnclos()).hasSize(1);

        final var enclos = parc.getEnclos().iterator().next();
        assertThat(enclos.getTypologie()).isEqualTo(Typologie.FORET);
        assertThat(enclos.getSurface()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(enclos.getRessources()).hasSize(2);

        assertThat(enclos.getRessources())
            .filteredOn(r -> r instanceof Nourriture)
            .hasSize(1)
            .first()
            .satisfies(r -> {
                final var n = (Nourriture) r;
                assertThat(n.getTypeNourriture()).isEqualTo(TypeNourriture.VEGETAL);
                assertThat(n.getQuantite()).isEqualByComparingTo(new BigDecimal("500.00"));
            });

        assertThat(enclos.getRessources())
            .filteredOn(r -> r instanceof Eau)
            .hasSize(1)
            .first()
            .satisfies(r -> {
                final var e = (Eau) r;
                assertThat(e.getTypeEau()).isEqualTo(TypeEau.DOUCE);
                assertThat(e.getQuantite()).isEqualByComparingTo(new BigDecimal("1000.00"));
            });
    }

    @Test
    void shouldReturnEmptyWhenNoParcMatchesClimat() {
        final var results = parcRepository.findCandidateParcs(List.of(Climat.TEMPERE), StatutParc.OUVERT);

        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenDinoDoesNotExist() {
        assertThat(parcRepository.existsDinoById(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldSaveDinoAndReportItExists() {
        final var dinoId = UUID.randomUUID();

        parcRepository.saveDino(dinoId, "TREX", foretEnclosId);

        assertThat(parcRepository.existsDinoById(dinoId)).isTrue();
    }

    @Test
    void shouldIncludeAssignedDinosInCandidateParcs() {
        final var dinoId = UUID.randomUUID();
        parcRepository.saveDino(dinoId, "VELOCIRAPTOR", foretEnclosId);

        final var results = parcRepository.findCandidateParcs(List.of(Climat.CHAUD), StatutParc.OUVERT);

        assertThat(results).hasSize(1);
        final var enclos = results.getFirst().getEnclos().iterator().next();
        assertThat(enclos.getDinos()).hasSize(1);
        assertThat(enclos.getDinos().iterator().next().getId()).isEqualTo(dinoId);
        assertThat(enclos.getDinos().iterator().next().getSpecies()).isEqualTo("VELOCIRAPTOR");
    }

    @Test
    void shouldNotReturnParcsThatAreNotOpen() {
        // FEAT-2077 : un parc non ouvert n'est jamais candidat
        jdbcClient.sql("INSERT INTO parc (nom, climat, statut) VALUES (?, ?, ?)")
            .params(NomParc.NOIRMOUTIER.name(), Climat.CHAUD.name(), StatutParc.CONSTRUCTION.name())
            .update();

        final var results = parcRepository.findCandidateParcs(List.of(Climat.CHAUD), StatutParc.OUVERT);

        assertThat(results)
            .extracting(p -> p.getNom())
            .containsExactly(NomParc.HAWAII);
    }

    @Test
    void shouldFindParcsByNomMatchingPattern() {
        final var results = parcRepository.findParcsByNomMatchingPattern("HAW.*");

        assertThat(results)
            .hasSize(1)
            .first()
            .satisfies(p -> assertThat(p.getNom()).isEqualTo(NomParc.HAWAII));
    }

    @Test
    void shouldReturnEmptyWhenNoParcMatchesPattern() {
        final var results = parcRepository.findParcsByNomMatchingPattern("BEL.*");

        assertThat(results).isEmpty();
    }
}
