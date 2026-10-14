package com.example.petclinic.service;

import com.example.petclinic.entity.Pet;
import com.example.petclinic.repository.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public List<Pet> findPets() {
        return petRepository.findAll();
    }

    public List<Pet> findPetsByYearAndType(Long year, String type) {
        LocalDateTime dateThreshold = LocalDate.now().minusYears(year).atStartOfDay();
        return petRepository.findAll().stream()
                .filter(pet -> pet.getBirthDate().isAfter(dateThreshold)
                        && pet.getType().getName().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public Pet findPet(Long petId) {
        return verifyPetExists(petId);
    }

    private Pet verifyPetExists(Long petId) {
        return petRepository.findById(petId).orElseThrow(
                () -> new NoSuchElementException("Pet does not exist")
        );
    }
}
