package fr.liksi.parcmanager.external;

import fr.liksi.parcmanager.external.dto.DinoTypeDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;

@Service
public class DinoTypeServiceImpl implements DinoTypeService {

    private final DinoTypeApiClient dinoTypeApiClient;

    public DinoTypeServiceImpl(DinoTypeApiClient dinoTypeApiClient) {
        this.dinoTypeApiClient = dinoTypeApiClient;
    }

    @Override
    public Optional<DinoTypeDto> getDinoType(UUID dinoId) {
        // Adaptation infra -> domaine : le 404 de l'API externe devient un Optional.empty()
        try {
            return Optional.ofNullable(dinoTypeApiClient.getDinoType(dinoId));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
