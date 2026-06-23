package fr.liksi.parcmanager.model.entity;

import java.util.UUID;

public class Dino {

    private UUID id;
    private String species;
    private Enclos enclos;

    public Dino() {
    }

    public Dino(UUID id, String species) {
        this.id = id;
        this.species = species;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public Enclos getEnclos() {
        return enclos;
    }

    public void setEnclos(Enclos enclos) {
        this.enclos = enclos;
    }
}
