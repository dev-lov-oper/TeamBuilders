package com.campus.matching.model;

import java.util.List;

public record StudentData(
    Long id,
    String name,
    List<String> skills,
    List<String> interests,
    List<String> roles,
    String experience
) {
    public StudentData {
        if (skills == null) skills = List.of();
        if (interests == null) interests = List.of();
        if (roles == null) roles = List.of();
        if (experience == null) experience = "BEGINNER";
    }
}
