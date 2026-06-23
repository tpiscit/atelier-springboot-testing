package fr.liksi.parcmanager.service.exception;

import java.util.UUID;

public class DinoAlreadyExistsException extends RuntimeException {

    public DinoAlreadyExistsException(UUID id) {
        super("Dino deja existant avec l'id: " + id);
    }
}
