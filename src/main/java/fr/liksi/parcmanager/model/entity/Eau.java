package fr.liksi.parcmanager.model.entity;

import fr.liksi.parcmanager.model.enums.TypeEau;

import java.math.BigDecimal;

public class Eau extends Ressource {

    private TypeEau typeEau;

    public Eau() {
    }

    public Eau(TypeEau typeEau, BigDecimal quantite) {
        super(quantite);
        this.typeEau = typeEau;
    }

    public TypeEau getTypeEau() {
        return typeEau;
    }
}
