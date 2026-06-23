package fr.liksi.parcmanager.model.entity;

import fr.liksi.parcmanager.model.enums.TypeNourriture;

import java.math.BigDecimal;

public class Nourriture extends Ressource {

    private TypeNourriture typeNourriture;

    public Nourriture() {
    }

    public Nourriture(TypeNourriture typeNourriture, BigDecimal quantite) {
        super(quantite);
        this.typeNourriture = typeNourriture;
    }

    public TypeNourriture getTypeNourriture() {
        return typeNourriture;
    }
}
