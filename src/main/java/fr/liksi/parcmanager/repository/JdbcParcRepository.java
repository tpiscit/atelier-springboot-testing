package fr.liksi.parcmanager.repository;

import fr.liksi.parcmanager.model.entity.*;
import fr.liksi.parcmanager.model.enums.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class JdbcParcRepository implements ParcRepository {

    private static final String SELECT_AGGREGATE_SQL = """
        SELECT
            p.id              AS parc_id,
            p.nom             AS parc_nom,
            p.climat          AS parc_climat,
            p.statut          AS parc_statut,
            e.id              AS enclos_id,
            e.typologie       AS enclos_typologie,
            e.surface         AS enclos_surface,
            r.id              AS ressource_id,
            r.type_ressource  AS ressource_type,
            r.quantite        AS ressource_quantite,
            r.type_nourriture AS ressource_type_nourriture,
            r.type_eau        AS ressource_type_eau,
            d.id              AS dino_id,
            d.species         AS dino_species
        FROM parc p
        LEFT JOIN enclos e   ON e.parc_id = p.id
        LEFT JOIN ressource r ON r.enclos_id = e.id
        LEFT JOIN dino d      ON d.enclos_id = e.id
        WHERE p.climat IN (:climats)
        """;

    private final JdbcClient jdbcClient;

    public JdbcParcRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Parc> findCandidateParcs(List<Climat> climats) {
        if (climats == null || climats.isEmpty()) {
            return List.of();
        }
        final var climatNames = climats.stream().map(Enum::name).toList();

        return jdbcClient.sql(SELECT_AGGREGATE_SQL)
            .param("climats", climatNames)
            .query(this::extractParcs);
    }

    public boolean existsDinoById(UUID id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM dino WHERE id = :id")
            .param("id", id)
            .query(Integer.class)
            .single() > 0;
    }

    public List<Parc> findParcsByNomMatchingPattern(String regexPattern) {
        return jdbcClient.sql("SELECT * FROM parc WHERE nom ~ :pattern")
            .param("pattern", regexPattern)
            .query((rs, ignoredRowNum) -> {
                final var parc = new Parc(
                    NomParc.valueOf(rs.getString("nom")),
                    Climat.valueOf(rs.getString("climat")),
                    StatutParc.valueOf(rs.getString("statut"))
                );
                parc.setId(rs.getLong("id"));
                return parc;
            })
            .list();
    }

    public void saveDino(UUID id, String species, Long enclosId) {
        jdbcClient.sql("INSERT INTO dino (id, species, enclos_id) VALUES (:id, :species, :enclosId)")
            .param("id", id)
            .param("species", species)
            .param("enclosId", enclosId)
            .update();
    }

    private List<Parc> extractParcs(ResultSet rs) throws SQLException {
        final var context = new ExtractionContext();

        while (rs.next()) {
            extractRow(rs, context);
        }

        return List.copyOf(context.parcsById.values());
    }

    private void extractRow(ResultSet rs, ExtractionContext context) throws SQLException {
        final var parc = context.parcsById.computeIfAbsent(rs.getLong("parc_id"), id -> mapParc(rs, id));

        final var enclosId = rs.getLong("enclos_id");
        if (rs.wasNull()) {
            return;
        }
        final var enclos = context.enclosById.computeIfAbsent(enclosId, id -> {
            final var newEnclos = mapEnclos(rs, id);
            parc.addEnclos(newEnclos);
            return newEnclos;
        });

        extractRessource(rs, context, enclos);
        extractDino(rs, context, enclos);
    }

    private void extractRessource(ResultSet rs, ExtractionContext context, Enclos enclos) throws SQLException {
        final var ressourceId = rs.getLong("ressource_id");
        if (rs.wasNull()) {
            return;
        }
        context.ressourcesById.computeIfAbsent(ressourceId, id -> {
            final var ressource = mapRessourceUnchecked(rs, id);
            enclos.addRessource(ressource);
            return ressource;
        });
    }

    private void extractDino(ResultSet rs, ExtractionContext context, Enclos enclos) throws SQLException {
        final var dinoIdStr = rs.getString("dino_id");
        if (dinoIdStr == null) {
            return;
        }
        context.dinosById.computeIfAbsent(UUID.fromString(dinoIdStr), id -> {
            final var dino = mapDino(rs, id);
            enclos.addDino(dino);
            return dino;
        });
    }

    private Dino mapDino(ResultSet rs, UUID id) {
        try {
            return new Dino(id, rs.getString("dino_species"));
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map Dino", e);
        }
    }

    private static final class ExtractionContext {
        private final Map<Long, Parc> parcsById = new LinkedHashMap<>();
        private final Map<Long, Enclos> enclosById = new HashMap<>();
        private final Map<Long, Ressource> ressourcesById = new HashMap<>();
        private final Map<UUID, Dino> dinosById = new HashMap<>();
    }

    private Parc mapParc(ResultSet rs, long id) {
        try {
            final var parc = new Parc(
                NomParc.valueOf(rs.getString("parc_nom")),
                Climat.valueOf(rs.getString("parc_climat")),
                StatutParc.valueOf(rs.getString("parc_statut"))
            );
            parc.setId(id);
            return parc;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map Parc", e);
        }
    }

    private Enclos mapEnclos(ResultSet rs, long id) {
        try {
            final var enclos = new Enclos(
                Typologie.valueOf(rs.getString("enclos_typologie")),
                rs.getBigDecimal("enclos_surface")
            );
            enclos.setId(id);
            return enclos;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map Enclos", e);
        }
    }

    private Ressource mapRessourceUnchecked(ResultSet rs, long id) {
        try {
            return mapRessource(rs, id);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to map Ressource", e);
        }
    }

    private Ressource mapRessource(ResultSet rs, long id) throws SQLException {
        final var type = rs.getString("ressource_type");
        final var quantite = rs.getBigDecimal("ressource_quantite");
        final var ressource = switch (type) {
            case "NOURRITURE" -> new Nourriture(
                TypeNourriture.valueOf(rs.getString("ressource_type_nourriture")),
                quantite
            );
            case "EAU" -> new Eau(
                TypeEau.valueOf(rs.getString("ressource_type_eau")),
                quantite
            );
            default -> throw new IllegalStateException("Unknown ressource type: " + type);
        };
        ressource.setId(id);
        return ressource;
    }
}
