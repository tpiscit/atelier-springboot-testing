package fr.liksi.parcmanager.web;

import fr.liksi.parcmanager.service.DinoAssignmentService;
import fr.liksi.parcmanager.web.dto.DinoAssignmentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dinos")
public class DinoAssignmentController {

    private final DinoAssignmentService dinoAssignmentService;

    public DinoAssignmentController(DinoAssignmentService dinoAssignmentService) {
        this.dinoAssignmentService = dinoAssignmentService;
    }

    @PostMapping("/assign")
    public ResponseEntity<DinoAssignmentDto> assignDino(@RequestBody UUID dinoId) {
        final var response = dinoAssignmentService.assignDino(dinoId);
        return ResponseEntity.ok(DinoAssignmentDto.fromDomain(response));
    }
}
