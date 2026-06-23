package fr.liksi.parcmanager.service.dto;

import fr.liksi.parcmanager.model.enums.TypeNourriture;

import java.math.BigDecimal;
import java.util.Map;

public record AvailableResources(
    BigDecimal availableWater,
    Map<TypeNourriture, BigDecimal> availableFoodByType,
    BigDecimal availableSurface
) {
}
