package fr.liksi.parcmanager.external;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.UUID;

public interface DinoTypeApiClient {

    @GetExchange("/api/dinotype")
    DinoTypeDto getDinoType(@RequestParam("id") UUID dinoId);
}
