package fr.liksi.parcmanager.service.dinospecies;

import fr.liksi.parcmanager.external.DinoTypeService;
import fr.liksi.parcmanager.model.entity.DinoSpecies;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DinoSpeciesServiceImpl implements DinoSpeciesService {

    private final DinoSpeciesRegistry dinoSpeciesRegistry;
    private final DinoTypeService dinoTypeService;

    public DinoSpeciesServiceImpl(DinoSpeciesRegistry dinoSpeciesRegistry, DinoTypeService dinoTypeService) {
        this.dinoSpeciesRegistry = dinoSpeciesRegistry;
        this.dinoTypeService = dinoTypeService;
    }

    @Override
    public Optional<DinoSpecies> findDinoSpeciesByDinoId(UUID dinoId) {
        return dinoTypeService.getDinoType(dinoId)
            .flatMap(dinoType -> dinoSpeciesRegistry.findBySpeciesName(dinoType.species()));
    }

    @Override
    public Optional<DinoSpecies> findDinoSpeciesBySpeciesName(String speciesName) {
        return dinoSpeciesRegistry.findBySpeciesName(speciesName);
    }
}
