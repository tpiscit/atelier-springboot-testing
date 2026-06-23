package fr.liksi.parcmanager.service.exception;

import java.util.UUID;

public class DinoNotFoundException extends RuntimeException {
    public DinoNotFoundException(UUID dinoId) {
        super("Dino not found: " + dinoId);
    }
}
