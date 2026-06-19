package fr.liksi.parcmanager;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de "warm-up" present UNIQUEMENT sur la branche main.
 *
 * <p>Objectif : pendant l'intro de l'atelier, lancer {@code ./mvnw test} permet
 * de telecharger toutes les dependances Maven ET de pre-telecharger (pull)
 * l'image Docker {@code postgres:16}. Cette image est ensuite reutilisee par
 * Testcontainers aux etapes qui migrent vers PostgreSQL
 * ({@code jdbc:tc:postgresql:16}), donc plus d'attente de telechargement a ce
 * moment-la.</p>
 *
 * <p>Ce fichier n'existe pas sur les branches {@code atelier/x-...} : il
 * disparait automatiquement des qu'on bascule sur une etape
 * ({@code git switch atelier/...}). Ne pas merger ce commit dans les etapes.</p>
 */
@Testcontainers
class DockerImageWarmupTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));

    @Test
    void preTelechargeEtDemarreLImagePostgres16() {
        // Le simple demarrage du conteneur force le pull de postgres:16.
        assertThat(POSTGRES.isRunning()).isTrue();
    }
}
