package fr.liksi.parcmanager.service;

import fr.liksi.parcmanager.service.dto.DinoAssignment;

import java.util.UUID;

public interface DinoAssignmentService {

    DinoAssignment assignDino(UUID dinoId);
}
