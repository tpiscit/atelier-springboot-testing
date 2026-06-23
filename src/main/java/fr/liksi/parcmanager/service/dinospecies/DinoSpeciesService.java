package fr.liksi.parcmanager.service.dinospecies;

import fr.liksi.parcmanager.model.entity.DinoSpecies;

import java.util.Optional;
import java.util.UUID;

public interface DinoSpeciesService {

    Optional<DinoSpecies> findDinoSpeciesByDinoId(UUID dinoId);

    Optional<DinoSpecies> findDinoSpeciesBySpeciesName(String speciesName);
}
