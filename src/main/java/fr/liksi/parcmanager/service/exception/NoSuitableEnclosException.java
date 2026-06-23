package fr.liksi.parcmanager.service.exception;

public class NoSuitableEnclosException extends RuntimeException {

    public NoSuitableEnclosException(String species) {
        super("Aucun enclos disponible pour l'espece: " + species);
    }
}
