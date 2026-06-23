package fr.liksi.parcmanager.service;

import fr.liksi.parcmanager.model.entity.*;
import fr.liksi.parcmanager.model.enums.TypeNourriture;
import fr.liksi.parcmanager.repository.ParcRepository;
import fr.liksi.parcmanager.service.dinospecies.DinoSpeciesService;
import fr.liksi.parcmanager.service.dto.AvailableResources;
import fr.liksi.parcmanager.service.dto.DinoAssignment;
import fr.liksi.parcmanager.service.dto.Dino;
import fr.liksi.parcmanager.service.dto.EnclosWithDinos;
import fr.liksi.parcmanager.service.exception.DinoAlreadyExistsException;
import fr.liksi.parcmanager.service.exception.NoSuitableEnclosException;
import fr.liksi.parcmanager.service.exception.SpeciesNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DinoAssignmentServiceImpl implements DinoAssignmentService {

    private final ParcRepository parcRepository;
    private final DinoSpeciesService dinoSpeciesService;

    public DinoAssignmentServiceImpl(ParcRepository parcRepository,
                                     DinoSpeciesService dinoSpeciesService) {
        this.parcRepository = parcRepository;
        this.dinoSpeciesService = dinoSpeciesService;
    }

    @Override
    public DinoAssignment assignDino(UUID dinoId) {
        if (parcRepository.existsDinoById(dinoId)) {
            throw new DinoAlreadyExistsException(dinoId);
        }

        final var species = dinoSpeciesService.findDinoSpeciesByDinoId(dinoId)
            .orElseThrow(() -> new SpeciesNotFoundException(dinoId.toString()));

        final var suitableEnclos = findSuitableEnclos(species);
        suitableEnclos.addDino(new fr.liksi.parcmanager.model.entity.Dino(dinoId, species.getSpeciesName()));
        parcRepository.saveDino(dinoId, species.getSpeciesName(), suitableEnclos.getId());
        return toAssignmentResponse(suitableEnclos.getParc());
    }

    private Enclos findSuitableEnclos(DinoSpecies species) {
        return parcRepository.findCandidateParcs(species.getPreferredClimates()).stream()
            .flatMap(parc -> parc.getEnclos().stream())
            .filter(enclos -> isEnclosSuitable(enclos, species))
            .findFirst()
            .orElseThrow(() -> new NoSuitableEnclosException(species.getSpeciesName()));
    }

    private boolean isEnclosSuitable(Enclos enclos, DinoSpecies species) {
        return species.getPreferredHabitats().contains(enclos.getTypologie())
            && calculateAvailableSurface(enclos).compareTo(species.getRequiredSurface()) >= 0
            && calculateAvailableWater(enclos).compareTo(species.getWaterQuantity()) >= 0
            && calculateAvailableFoodByType(enclos, species.getFoodType()).compareTo(species.getFoodQuantity()) >= 0;
    }

    private BigDecimal calculateAvailableSurface(Enclos enclos) {
        final var usedSurface = enclos.getDinos().stream()
            .map(dino -> dinoSpeciesService.findDinoSpeciesBySpeciesName(dino.getSpecies()))
            .flatMap(Optional::stream)
            .map(DinoSpecies::getRequiredSurface)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return enclos.getSurface().subtract(usedSurface);
    }

    private BigDecimal calculateAvailableWater(Enclos enclos) {
        final var totalWater = enclos.getRessources().stream()
            .filter(r -> r instanceof Eau)
            .map(Ressource::getQuantite)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var consumedWater = enclos.getDinos().stream()
            .map(dino -> dinoSpeciesService.findDinoSpeciesBySpeciesName(dino.getSpecies()))
            .flatMap(Optional::stream)
            .map(DinoSpecies::getWaterQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalWater.subtract(consumedWater);
    }

    private BigDecimal calculateAvailableFoodByType(Enclos enclos, TypeNourriture type) {
        final var totalFood = enclos.getRessources().stream()
            .filter(r -> r instanceof Nourriture)
            .map(r -> (Nourriture) r)
            .filter(n -> n.getTypeNourriture() == type)
            .map(Ressource::getQuantite)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        final var consumedFood = enclos.getDinos().stream()
            .map(dino -> dinoSpeciesService.findDinoSpeciesBySpeciesName(dino.getSpecies()))
            .flatMap(Optional::stream)
            .filter(s -> s.getFoodType() == type)
            .map(DinoSpecies::getFoodQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalFood.subtract(consumedFood);
    }

    private DinoAssignment toAssignmentResponse(Parc parc) {
        final var enclosDtos = parc.getEnclos().stream()
            .map(this::toEnclosWithDinosDto)
            .collect(Collectors.toSet());

        return new DinoAssignment(parc.getNom(), parc.getClimat(), enclosDtos);
    }

    private EnclosWithDinos toEnclosWithDinosDto(Enclos enclos) {
        final var dinos = enclos.getDinos().stream()
            .map(dino -> new Dino(dino.getId(), dino.getSpecies()))
            .collect(Collectors.toSet());

        return new EnclosWithDinos(
            enclos.getId(),
            enclos.getTypologie(),
            enclos.getSurface(),
            dinos,
            calculateAvailableResources(enclos)
        );
    }

    private AvailableResources calculateAvailableResources(Enclos enclos) {
        final var availableFoodByType = Arrays.stream(TypeNourriture.values())
            .map(type -> Map.entry(type, calculateAvailableFoodByType(enclos, type)))
            .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return new AvailableResources(
            calculateAvailableWater(enclos),
            availableFoodByType,
            calculateAvailableSurface(enclos)
        );
    }
}
