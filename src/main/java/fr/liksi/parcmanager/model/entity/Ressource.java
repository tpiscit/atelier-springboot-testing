package fr.liksi.parcmanager.model.entity;

import java.math.BigDecimal;

public abstract class Ressource {

    private Long id;
    private BigDecimal quantite;
    private Enclos enclos;

    protected Ressource() {
    }

    protected Ressource(BigDecimal quantite) {
        this.quantite = quantite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public Enclos getEnclos() {
        return enclos;
    }

    public void setEnclos(Enclos enclos) {
        this.enclos = enclos;
    }
}
