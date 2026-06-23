package fr.liksi.parcmanager.model.entity;

import fr.liksi.parcmanager.model.enums.Typologie;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class Enclos {

    private Long id;
    private Typologie typologie;
    private BigDecimal surface;
    private Parc parc;
    private final Set<Ressource> ressources = new HashSet<>();
    private Set<Dino> dinos = new HashSet<>();

    public Enclos() {
    }

    public Enclos(Typologie typologie, BigDecimal surface) {
        this.typologie = typologie;
        this.surface = surface;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Typologie getTypologie() {
        return typologie;
    }

    public void setTypologie(Typologie typologie) {
        this.typologie = typologie;
    }

    public BigDecimal getSurface() {
        return surface;
    }

    public Parc getParc() {
        return parc;
    }

    public void setParc(Parc parc) {
        this.parc = parc;
    }

    public Set<Ressource> getRessources() {
        return ressources;
    }

    public void addRessource(Ressource ressource) {
        this.ressources.add(ressource);
        ressource.setEnclos(this);
    }

    public Set<Dino> getDinos() {
        return dinos;
    }

    public void setDinos(Set<Dino> dinos) {
        this.dinos = dinos;
    }

    public void addDino(Dino dino) {
        this.dinos.add(dino);
        dino.setEnclos(this);
    }
}
