package fr.liksi.parcmanager.web;

import fr.liksi.parcmanager.service.exception.DinoAlreadyExistsException;
import fr.liksi.parcmanager.service.exception.DinoNotFoundException;
import fr.liksi.parcmanager.service.exception.NoSuitableEnclosException;
import fr.liksi.parcmanager.service.exception.SpeciesNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuitableEnclosException.class)
    public ProblemDetail handleNoSuitableEnclos(NoSuitableEnclosException ex) {
        final var problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problemDetail.setTitle("Affectation impossible");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(SpeciesNotFoundException.class)
    public ProblemDetail handleSpeciesNotFound(SpeciesNotFoundException ex) {
        final var problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Espece non trouvee");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DinoAlreadyExistsException.class)
    public ProblemDetail handleDinoAlreadyExists(DinoAlreadyExistsException ex) {
        final var problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Dino deja existant");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DinoNotFoundException.class)
    public ProblemDetail handleDinoNotFound(DinoNotFoundException ex) {
        final var problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Dino non trouve");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }
}
