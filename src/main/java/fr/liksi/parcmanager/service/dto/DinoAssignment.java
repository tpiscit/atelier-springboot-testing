package fr.liksi.parcmanager.service.dto;

import fr.liksi.parcmanager.model.enums.Climat;
import fr.liksi.parcmanager.model.enums.NomParc;

import java.util.Set;

public record DinoAssignment(
    NomParc parcName,
    Climat climat,
    Set<EnclosWithDinos> enclos
) {
}
