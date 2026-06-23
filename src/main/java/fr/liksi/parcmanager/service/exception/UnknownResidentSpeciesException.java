package fr.liksi.parcmanager.service.exception;

/**
 * Levée quand un dino déjà présent dans un enclos référence une espèce
 * inconnue du registry : les ressources qu'il consomme ne peuvent pas être
 * calculées, ignorer silencieusement ce résident conduirait à sur-remplir
 * l'enclos (cf. BUG-1042).
 */
public class UnknownResidentSpeciesException extends RuntimeException {

    public UnknownResidentSpeciesException(String speciesName) {
        super("Espece inconnue pour un dino resident: " + speciesName);
    }
}
