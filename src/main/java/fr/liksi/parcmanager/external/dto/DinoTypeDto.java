package fr.liksi.parcmanager.external.dto;

import java.util.UUID;

public record DinoTypeDto(
    UUID guid,
    String species,
    String family
) {
}
