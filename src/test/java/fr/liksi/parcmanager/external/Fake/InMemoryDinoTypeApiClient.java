package fr.liksi.parcmanager.external.Fake;

import fr.liksi.parcmanager.external.DinoTypeApiClient;
import fr.liksi.parcmanager.external.dto.DinoTypeDto;

import java.util.*;

public class InMemoryDinoTypeApiClient implements DinoTypeApiClient {

    private Map<UUID, DinoTypeDto> dinoTypeDtos = new HashMap<>();

    public void AddDinoType(DinoTypeDto dto) {
        dinoTypeDtos.put(dto.guid(),dto);
    }

    @Override
    public DinoTypeDto getDinoType(UUID dinoId) {
        return dinoTypeDtos.get(dinoId);
    }
}
