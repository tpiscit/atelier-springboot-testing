package fr.liksi.parcmanager.repository;

import fr.liksi.parcmanager.model.entity.Parc;
import fr.liksi.parcmanager.model.enums.Climat;

import java.util.List;
import java.util.UUID;

public interface ParcRepository {

    List<Parc> findCandidateParcs(List<Climat> climats);

    boolean existsDinoById(UUID id);

    List<Parc> findParcsByNomMatchingPattern(String regexPattern);

    void saveDino(UUID id, String species, Long enclosId);
}
