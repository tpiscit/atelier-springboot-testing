package fr.liksi.parcmanager.service.exception;

public class SpeciesNotFoundException extends RuntimeException {

    public SpeciesNotFoundException(String species) {
        super("Espece non trouvee: " + species);
    }
}
