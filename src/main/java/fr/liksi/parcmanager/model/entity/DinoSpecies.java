package fr.liksi.parcmanager.model.entity;

import fr.liksi.parcmanager.model.enums.Climat;
import fr.liksi.parcmanager.model.enums.TypeNourriture;
import fr.liksi.parcmanager.model.enums.Typologie;

import java.math.BigDecimal;
import java.util.List;

public class DinoSpecies {

    private String speciesName;
    private TypeNourriture foodType;
    private BigDecimal foodQuantity;
    private BigDecimal waterQuantity;
    private List<Typologie> preferredHabitats;
    private BigDecimal requiredSurface;
    private List<Climat> preferredClimates;

    public DinoSpecies() {
    }

    public DinoSpecies(String speciesName, TypeNourriture foodType, BigDecimal foodQuantity,
                       BigDecimal waterQuantity, List<Typologie> preferredHabitats,
                       BigDecimal requiredSurface, List<Climat> preferredClimates) {
        this.speciesName = speciesName;
        this.foodType = foodType;
        this.foodQuantity = foodQuantity;
        this.waterQuantity = waterQuantity;
        this.preferredHabitats = preferredHabitats;
        this.requiredSurface = requiredSurface;
        this.preferredClimates = preferredClimates;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public TypeNourriture getFoodType() {
        return foodType;
    }

    public BigDecimal getFoodQuantity() {
        return foodQuantity;
    }

    public BigDecimal getWaterQuantity() {
        return waterQuantity;
    }

    public List<Typologie> getPreferredHabitats() {
        return preferredHabitats;
    }

    public BigDecimal getRequiredSurface() {
        return requiredSurface;
    }

    public List<Climat> getPreferredClimates() {
        return preferredClimates;
    }
}
