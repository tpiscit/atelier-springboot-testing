package fr.liksi.parcmanager.web.dto;

import fr.liksi.parcmanager.model.enums.Climat;
import fr.liksi.parcmanager.model.enums.NomParc;
import fr.liksi.parcmanager.service.dto.DinoAssignment;

import java.util.Set;
import java.util.stream.Collectors;

public record DinoAssignmentDto(NomParc parcName,
                                Climat climat,
                                Set<EnclosWithDinosDto> enclos) {

    public static DinoAssignmentDto fromDomain(DinoAssignment assignment) {
        return new DinoAssignmentDto(
            assignment.parcName(),
            assignment.climat(),
            assignment.enclos().stream().map(EnclosWithDinosDto::fromDomain).collect(Collectors.toSet())
        );
    }
}
