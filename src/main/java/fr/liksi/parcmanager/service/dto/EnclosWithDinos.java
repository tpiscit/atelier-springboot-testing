package fr.liksi.parcmanager.service.dto;

import fr.liksi.parcmanager.model.enums.Typologie;

import java.math.BigDecimal;
import java.util.Set;

public record EnclosWithDinos(
    Long id,
    Typologie typologie,
    BigDecimal surface,
    Set<Dino> dinos,
    AvailableResources availableResources
) {
}
