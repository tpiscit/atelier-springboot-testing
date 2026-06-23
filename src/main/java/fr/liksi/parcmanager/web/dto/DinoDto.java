package fr.liksi.parcmanager.web.dto;

import fr.liksi.parcmanager.service.dto.Dino;

import java.util.UUID;

public record DinoDto(UUID id, String species) {

    public static DinoDto fromDomain(Dino dino) {
        return new DinoDto(dino.id(), dino.species());
    }
}
