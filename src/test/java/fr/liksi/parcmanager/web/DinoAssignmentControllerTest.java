package fr.liksi.parcmanager.web;

import fr.liksi.parcmanager.config.SecurityConfig;
import fr.liksi.parcmanager.model.enums.Climat;
import fr.liksi.parcmanager.model.enums.NomParc;
import fr.liksi.parcmanager.model.enums.TypeNourriture;
import fr.liksi.parcmanager.model.enums.Typologie;
import fr.liksi.parcmanager.service.DinoAssignmentService;
import fr.liksi.parcmanager.service.dto.AvailableResources;
import fr.liksi.parcmanager.service.dto.DinoAssignment;
import fr.liksi.parcmanager.service.dto.Dino;
import fr.liksi.parcmanager.service.dto.EnclosWithDinos;
import fr.liksi.parcmanager.service.exception.DinoNotFoundException;
import fr.liksi.parcmanager.service.exception.NoSuitableEnclosException;
import fr.liksi.parcmanager.service.exception.SpeciesNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DinoAssignmentController.class)
@Import(SecurityConfig.class)
class DinoAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DinoAssignmentService dinoAssignmentService;

    @Test
    void shouldAssignDinoSuccessfully() throws Exception {
        // Given
        final var dinoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        final var species = "TREX";

        final var response = createSuccessResponse(dinoId, species);

        when(dinoAssignmentService.assignDino(any(UUID.class)))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/dinos/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"550e8400-e29b-41d4-a716-446655440000\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.parcName").value("HAWAII"))
            .andExpect(jsonPath("$.climat").value("CHAUD"))
            .andExpect(jsonPath("$.enclos").isArray())
            .andExpect(jsonPath("$.enclos[0].id").value(1))
            .andExpect(jsonPath("$.enclos[0].typologie").value("PLAINE"))
            .andExpect(jsonPath("$.enclos[0].surface").value(1000))
            .andExpect(jsonPath("$.enclos[0].dinos").isArray())
            .andExpect(jsonPath("$.enclos[0].dinos.length()").value(1))
            .andExpect(jsonPath("$.enclos[0].dinos[0].id").value("550e8400-e29b-41d4-a716-446655440000"))
            .andExpect(jsonPath("$.enclos[0].dinos[0].species").value("TREX"))
            .andExpect(jsonPath("$.enclos[0].availableResources.availableWater").value(450))
            .andExpect(jsonPath("$.enclos[0].availableResources.availableSurface").value(800))
            .andExpect(jsonPath("$.enclos[0].availableResources.availableFoodByType.ANIMAL").value(100));

        verify(dinoAssignmentService).assignDino(any(UUID.class));
    }

    @Test
    void shouldReturn404WhenSpeciesNotFound() throws Exception {
        // Given
        when(dinoAssignmentService.assignDino(any(UUID.class)))
            .thenThrow(new SpeciesNotFoundException("UNKNOWN"));

        // When & Then
        mockMvc.perform(post("/api/dinos/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"550e8400-e29b-41d4-a716-446655440000\""))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Espece non trouvee"))
            .andExpect(jsonPath("$.detail").value("Espece non trouvee: UNKNOWN"));
    }

    @Test
    void shouldReturn422WhenNoSuitableEnclosFound() throws Exception {
        // Given
        when(dinoAssignmentService.assignDino(any(UUID.class)))
            .thenThrow(new NoSuitableEnclosException("TREX"));

        // When & Then
        mockMvc.perform(post("/api/dinos/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"550e8400-e29b-41d4-a716-446655440000\""))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.title").value("Affectation impossible"))
            .andExpect(jsonPath("$.detail").value("Aucun enclos disponible pour l'espece: TREX"));
    }

    @Test
    void shouldAcceptValidRequest() throws Exception {
        // Given
        final var dinoId = UUID.randomUUID();
        final var response = createSuccessResponse(dinoId, "VELOCIRAPTOR");

        when(dinoAssignmentService.assignDino(any(UUID.class)))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/dinos/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("\"%s\"", dinoId)))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenDinoNotFoundInExternalApi() throws Exception {
        // Given
        final var dinoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        when(dinoAssignmentService.assignDino(any(UUID.class)))
            .thenThrow(new DinoNotFoundException(dinoId));

        // When & Then
        mockMvc.perform(post("/api/dinos/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"550e8400-e29b-41d4-a716-446655440000\""))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Dino non trouve"));
    }

    private DinoAssignment createSuccessResponse(UUID dinoId, String species) {
        final var dino = new Dino(dinoId, species);

        final var availableResources = new AvailableResources(
            BigDecimal.valueOf(450),
            Map.of(TypeNourriture.ANIMAL, BigDecimal.valueOf(100)),
            BigDecimal.valueOf(800)
        );

        final var enclosDto = new EnclosWithDinos(
            1L,
            Typologie.PLAINE,
            BigDecimal.valueOf(1000),
            Set.of(dino),
            availableResources
        );

        return new DinoAssignment(
            NomParc.HAWAII,
            Climat.CHAUD,
            Set.of(enclosDto)
        );
    }
}
