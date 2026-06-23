package fr.liksi.parcmanager.repository;

import fr.liksi.parcmanager.model.entity.Parc;
import fr.liksi.parcmanager.model.enums.Climat;
import fr.liksi.parcmanager.model.enums.StatutParc;

import java.util.*;

public class InMemoryParcRepository implements ParcRepository {

    private final List<Parc> parcs = new ArrayList<>();
    private final Set<UUID> dinoIds = new HashSet<>();

    public void givenParc(Parc parc) {
        parcs.add(parc);
    }

    public void givenDinoDejaAffecte(UUID id) {
        dinoIds.add(id);
    }

    @Override
    public List<Parc> findCandidateParcs(List<Climat> climats, StatutParc statut) {
        if (climats == null || climats.isEmpty()) {
            return List.of();
        }
        return parcs.stream()
            .filter(parc -> climats.contains(parc.getClimat()))
            .filter(parc -> parc.getStatut() == statut)
            .toList();
    }

    @Override
    public boolean existsDinoById(UUID id) {
        return dinoIds.contains(id);
    }

    @Override
    public List<Parc> findParcsByNomMatchingPattern(String regexPattern) {
        return parcs.stream()
            .filter(parc -> parc.getNom().name().matches(regexPattern))
            .toList();
    }

    @Override
    public void saveDino(UUID id, String species, Long enclosId) {
        dinoIds.add(id);
    }
}
