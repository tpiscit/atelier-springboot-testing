package fr.liksi.parcmanager.external;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryDinoTypeService implements DinoTypeService {

    private final Map<UUID, DinoTypeDto> dinos = new HashMap<>();

    public UUID givenDino(UUID dinoId, String species) {
        dinos.put(dinoId, new DinoTypeDto(dinoId, species, "Theropoda"));
        return dinoId;
    }

    @Override
    public Optional<DinoTypeDto> getDinoType(UUID dinoId) {
        return Optional.ofNullable(dinos.get(dinoId));
    }
}
