package com.example.petclinic.controller.view;

import com.example.petclinic.entity.Owner;
import com.example.petclinic.entity.Pet;
import com.example.petclinic.entity.Visit;
import com.example.petclinic.service.OwnerService;
import com.example.petclinic.service.PetService;
import com.example.petclinic.service.PetTypeService;
import com.example.petclinic.service.VetService;
import com.example.petclinic.service.VisitService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/** 사용자 화면(Thymeleaf). 조회 전용이며 로그인/사용자 관리는 없다. */
@Controller
public class WebController {

    private final OwnerService ownerService;
    private final PetService petService;
    private final PetTypeService petTypeService;
    private final VetService vetService;
    private final VisitService visitService;

    public WebController(OwnerService ownerService, PetService petService,
                         PetTypeService petTypeService, VetService vetService,
                         VisitService visitService) {
        this.ownerService = ownerService;
        this.petService = petService;
        this.petTypeService = petTypeService;
        this.vetService = vetService;
        this.visitService = visitService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Visit> visits = recentFirst(visitService.findVisits());
        model.addAttribute("ownerCount", ownerService.findOwners().size());
        model.addAttribute("petCount", petService.findPets().size());
        model.addAttribute("vetCount", vetService.findVets().size());
        model.addAttribute("visitCount", visits.size());
        model.addAttribute("recentVisits", visits.stream().limit(4).toList());
        model.addAttribute("petTypes", petTypeService.findPetTypes());
        model.addAttribute("active", "home");
        return "home";
    }

    /** 보호자 목록. lastname 이 있으면 해당 성으로 시작하는 보호자만 조회한다. */
    @GetMapping("/owners")
    public String owners(@RequestParam(defaultValue = "") String lastname, Model model) {
        String keyword = lastname.trim();
        List<Owner> owners = keyword.isEmpty()
                ? ownerService.findOwners()
                : ownerService.findOwnersByLastname(keyword);
        model.addAttribute("owners", owners);
        model.addAttribute("lastname", keyword);
        model.addAttribute("active", "owners");
        return "owners/list";
    }

    @GetMapping("/owners/{id}")
    public String owner(@PathVariable Long id, Model model) {
        Owner owner = ownerService.findOwner(id);
        List<Pet> pets = petService.findPets().stream()
                .filter(p -> p.getOwner().getId().equals(owner.getId()))
                .toList();
        model.addAttribute("owner", owner);
        model.addAttribute("pets", pets);
        model.addAttribute("active", "owners");
        return "owners/detail";
    }

    /**
     * 반려동물 목록. year(n년 미만)와 type(종류)을 함께 주면 예방접종 대상을 조회한다.
     * 하나만 주거나 year 가 올바르지 않으면 오류 대신 안내 문구와 함께 전체를 보여준다.
     */
    @GetMapping("/pets")
    public String pets(@RequestParam(defaultValue = "") String year,
                       @RequestParam(defaultValue = "") String type,
                       Model model) {
        String yearValue = year.trim();
        String typeValue = type.trim();
        List<Pet> pets;
        boolean filtered = false;
        String notice = null;

        if (yearValue.isEmpty() && typeValue.isEmpty()) {
            pets = petService.findPets();
        } else if (yearValue.isEmpty() || typeValue.isEmpty()) {
            pets = petService.findPets();
            notice = "나이와 종류를 함께 선택해 주세요.";
        } else {
            Long years = parsePositive(yearValue);
            if (years == null) {
                pets = petService.findPets();
                notice = "나이는 1 이상의 숫자로 입력해 주세요.";
                yearValue = ""; // 입력칸은 기본값(3)으로 되돌린다
            } else {
                pets = petService.findPetsByYearAndType(years, typeValue);
                filtered = true;
            }
        }

        model.addAttribute("pets", pets);
        model.addAttribute("filtered", filtered);
        model.addAttribute("notice", notice);
        model.addAttribute("year", yearValue);
        model.addAttribute("type", typeValue);
        model.addAttribute("petTypes", petTypeService.findPetTypes());
        model.addAttribute("active", "pets");
        return "pets/list";
    }

    @GetMapping("/pets/{id}")
    public String pet(@PathVariable Long id, Model model) {
        Pet pet = petService.findPet(id);
        List<Visit> visits = recentFirst(visitService.findVisits().stream()
                .filter(v -> v.getPet().getId().equals(pet.getId()))
                .toList());
        model.addAttribute("pet", pet);
        model.addAttribute("visits", visits);
        model.addAttribute("active", "pets");
        return "pets/detail";
    }

    @GetMapping("/vets")
    public String vets(Model model) {
        model.addAttribute("vets", vetService.findVets());
        model.addAttribute("active", "vets");
        return "vets";
    }

    @GetMapping("/visits")
    public String visits(@RequestParam(defaultValue = "") String q, Model model) {
        List<Visit> visits = recentFirst(visitService.findVisits()).stream()
                .filter(v -> matches(q, v.getDescription(), v.getPet().getName(),
                        v.getPet().getOwner().getFirstName(), v.getPet().getOwner().getLastName()))
                .toList();
        model.addAttribute("visits", visits);
        model.addAttribute("q", q);
        model.addAttribute("active", "visits");
        return "visits";
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound() {
        return "error/404";
    }

    private static Long parsePositive(String value) {
        try {
            long n = Long.parseLong(value);
            return n >= 1 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<Visit> recentFirst(List<Visit> visits) {
        return visits.stream()
                .sorted(Comparator.comparing(Visit::getDate).reversed())
                .toList();
    }

    private static boolean matches(String q, String... fields) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String kw = q.trim().toLowerCase(Locale.ROOT);
        for (String f : fields) {
            if (f != null && f.toLowerCase(Locale.ROOT).contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
