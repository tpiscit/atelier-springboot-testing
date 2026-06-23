package fr.liksi.parcmanager.external;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;

import java.util.Optional;
import java.util.UUID;

public interface DinoTypeService {

    /**
     * Récupère les informations d'un dinosaure depuis l'API externe.
     *
     * @param dinoId l'UUID du dinosaure
     * @return les informations du dinosaure, ou {@link Optional#empty()} s'il n'existe pas dans l'API externe
     */
    Optional<DinoTypeDto> getDinoType(UUID dinoId);
}
