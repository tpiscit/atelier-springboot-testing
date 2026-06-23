package fr.liksi.parcmanager.web.dto;

import fr.liksi.parcmanager.model.enums.Typologie;
import fr.liksi.parcmanager.service.dto.EnclosWithDinos;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public record EnclosWithDinosDto(Long id,
                                 Typologie typologie,
                                 BigDecimal surface,
                                 Set<DinoDto> dinos,
                                 AvailableResourcesDto availableResources) {

    public static EnclosWithDinosDto fromDomain(EnclosWithDinos enclos) {
        return new EnclosWithDinosDto(
            enclos.id(),
            enclos.typologie(),
            enclos.surface(),
            enclos.dinos().stream().map(DinoDto::fromDomain).collect(Collectors.toSet()),
            AvailableResourcesDto.fromDomain(enclos.availableResources())
        );
    }
}
