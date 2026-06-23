package fr.liksi.parcmanager.model.entity;

import fr.liksi.parcmanager.model.enums.Climat;
import fr.liksi.parcmanager.model.enums.NomParc;
import fr.liksi.parcmanager.model.enums.StatutParc;

import java.util.HashSet;
import java.util.Set;

public class Parc {

    private Long id;
    private NomParc nom;
    private Climat climat;
    private StatutParc statut;
    private Set<Enclos> enclos = new HashSet<>();

    public Parc() {
    }

    public Parc(NomParc nom, Climat climat, StatutParc statut) {
        this.nom = nom;
        this.climat = climat;
        this.statut = statut;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NomParc getNom() {
        return nom;
    }

    public Climat getClimat() {
        return climat;
    }

    public void setClimat(Climat climat) {
        this.climat = climat;
    }

    public StatutParc getStatut() {
        return statut;
    }

    public Set<Enclos> getEnclos() {
        return enclos;
    }

    public void setEnclos(Set<Enclos> enclos) {
        this.enclos = enclos;
    }

    public void addEnclos(Enclos enclos) {
        this.enclos.add(enclos);
        enclos.setParc(this);
    }
}
