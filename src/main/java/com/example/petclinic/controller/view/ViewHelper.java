package com.example.petclinic.controller.view;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;

/** 화면(Thymeleaf)에서 쓰는 표시용 도우미. 템플릿에서 {@code @viewHelper} 로 접근한다. */
@Component("viewHelper")
public class ViewHelper {

    private static final Map<String, String> EMOJI = Map.of(
            "cat", "🐱", "dog", "🐶", "lizard", "🦎",
            "snake", "🐍", "bird", "🐦", "hamster", "🐹");

    private static final Map<String, String> TYPE_KO = Map.of(
            "cat", "고양이", "dog", "강아지", "lizard", "도마뱀",
            "snake", "뱀", "bird", "새", "hamster", "햄스터");

    public String emoji(String type) {
        return type == null ? "🐾" : EMOJI.getOrDefault(type.toLowerCase(), "🐾");
    }

    public String typeKo(String type) {
        return type == null ? "" : TYPE_KO.getOrDefault(type.toLowerCase(), type);
    }

    public String age(LocalDateTime birthDate) {
        if (birthDate == null) {
            return "";
        }
        int years = Period.between(birthDate.toLocalDate(), LocalDate.now()).getYears();
        return years < 1 ? "1살 미만" : years + "살";
    }

    public String phone(String telephone) {
        if (telephone == null) {
            return "-";
        }
        String d = telephone.replaceAll("\\D", "");
        return d.length() == 10
                ? d.substring(0, 3) + "-" + d.substring(3, 6) + "-" + d.substring(6)
                : telephone;
    }

    public String initial(String name) {
        return name == null || name.isBlank() ? "?" : name.substring(0, 1).toUpperCase();
    }
}
