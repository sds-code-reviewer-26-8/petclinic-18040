package com.example.petclinic.controller;

import com.example.petclinic.controller.response.OwnerDto;
import com.example.petclinic.controller.response.PetDto;
import com.example.petclinic.controller.response.PetTypeDto;
import com.example.petclinic.entity.Pet;
import com.example.petclinic.service.PetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

        private final PetService petService;

        public PetController(PetService petService) {
                this.petService = petService;
        }

        @GetMapping
        public List<PetDto> list(
                        @RequestParam(value = "year", defaultValue = "") String year,
                        @RequestParam(value = "type", defaultValue = "") String type) {
                List<Pet> pets;
                if (year.isEmpty() && type.isEmpty()) {
                        pets = petService.findPets();
                } else if (year.isEmpty() || type.isEmpty()) {
                        throw new IllegalArgumentException("year or type parameter is required.");
                } else {
                        pets = petService.findPetsByYearAndType(Long.parseLong(year), type);
                }
                return pets.stream()
                                .map(p -> {
                                        PetTypeDto petType = new PetTypeDto(
                                                        p.getType().getId(),
                                                        p.getType().getName());
                                        OwnerDto owner = new OwnerDto(
                                                        p.getOwner().getId(),
                                                        p.getOwner().getFirstName(),
                                                        p.getOwner().getLastName(),
                                                        p.getOwner().getAddress(),
                                                        p.getOwner().getCity(),
                                                        p.getOwner().getTelephone());
                                        return new PetDto(
                                                        p.getId(),
                                                        p.getName(),
                                                        p.getBirthDate(),
                                                        petType,
                                                        owner);
                                })
                                .collect(Collectors.toList());
        }

        @GetMapping("/{id}")
        public PetDto detail(@PathVariable Long id) {
                Pet p = petService.findPet(id);

                PetTypeDto petType = new PetTypeDto(
                                p.getType().getId(),
                                p.getType().getName());
                OwnerDto owner = new OwnerDto(
                                p.getOwner().getId(),
                                p.getOwner().getFirstName(),
                                p.getOwner().getLastName(),
                                p.getOwner().getAddress(),
                                p.getOwner().getCity(),
                                p.getOwner().getTelephone());
                return new PetDto(
                                p.getId(),
                                p.getName(),
                                p.getBirthDate(),
                                petType,
                                owner);
        }
}
