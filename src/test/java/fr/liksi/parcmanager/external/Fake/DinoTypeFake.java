package fr.liksi.parcmanager.external.Fake;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Jeu de données en mémoire pour les TU DinoTypeService.
 *
 * <p>Joue le rôle de "base de données" côté test : le test y ajoute des
 * enregistrements, le {@link DinoTypeMockWebServerDispatcher} y lit pour
 * construire les réponses HTTP renvoyées par le MockWebServer.
 *
 * <p>Utilisation type :
 * <pre>{@code
 * // cas nominal
 * UUID id = UUID.randomUUID();
 * Fake.add(new DinoTypeDto(id, "TREX", "Theropoda"));
 *
 * // cas erreur serveur
 * Fake.simulateServerError();
 * }</pre>
 */
public class DinoTypeFake {

    private final Map<UUID, DinoTypeDto> store = new HashMap<>();
    private boolean serverError = false;

    /** Enregistre un dinosaure dans le jeu de données. */
    public void add(DinoTypeDto dto) {
        store.put(dto.guid(), dto);
    }

    /** Retourne le dinosaure correspondant à l'identifiant, ou vide si inconnu. */
    public Optional<DinoTypeDto> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Active le mode erreur serveur : toute requête suivante reçoit HTTP 500.
     * Réinitialisé par {@link #reset()}.
     */
    public void simulateServerError() {
        this.serverError = true;
    }

    /** Indique si le mode erreur serveur est actif. */
    public boolean isServerError() {
        return serverError;
    }

    /** Vide le jeu de données et désactive le mode erreur serveur. */
    public void reset() {
        store.clear();
        serverError = false;
    }
}
