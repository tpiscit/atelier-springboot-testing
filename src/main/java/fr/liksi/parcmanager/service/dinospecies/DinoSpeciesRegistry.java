package fr.liksi.parcmanager.service.dinospecies;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.liksi.parcmanager.model.entity.DinoSpecies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DinoSpeciesRegistry {

    private final Map<String, DinoSpecies> speciesByName;

    public DinoSpeciesRegistry() {
        this(new ClassPathResource("db/changelog/dino-species.json"));
    }

    public DinoSpeciesRegistry(
        @Value("classpath:db/changelog/dino-species.json") Resource speciesResource
    ) {
        this.speciesByName = loadFromJson(speciesResource);
    }

    public Optional<DinoSpecies> findBySpeciesName(String speciesName) {
        if (speciesName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(speciesByName.get(speciesName.toUpperCase(Locale.ROOT)));
    }

    private static Map<String, DinoSpecies> loadFromJson(Resource resource) {
        try (final var is = resource.getInputStream()) {
            final List<DinoSpecies> all = new ObjectMapper().readValue(is, new TypeReference<>() {});
            return all.stream()
                .collect(Collectors.toUnmodifiableMap(
                    species -> species.getSpeciesName().toUpperCase(Locale.ROOT),
                    Function.identity()
                ));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dino species from " + resource, e);
        }
    }
}
