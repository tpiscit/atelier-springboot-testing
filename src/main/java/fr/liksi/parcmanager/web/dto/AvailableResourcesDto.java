package fr.liksi.parcmanager.web.dto;

import fr.liksi.parcmanager.model.enums.TypeNourriture;
import fr.liksi.parcmanager.service.dto.AvailableResources;

import java.math.BigDecimal;
import java.util.Map;

public record AvailableResourcesDto(
    BigDecimal availableWater,
    Map<TypeNourriture, BigDecimal> availableFoodByType,
    BigDecimal availableSurface
) {

    public static AvailableResourcesDto fromDomain(AvailableResources resources) {
        return new AvailableResourcesDto(
            resources.availableWater(),
            resources.availableFoodByType(),
            resources.availableSurface()
        );
    }
}
