package com.campus.matching.model;

import java.util.List;
import java.util.Map;

public record ProjectData(
    Long id,
    String name,
    String projectType,
    int teamSize,
    List<String> requiredSkills,
    List<String> requiredRoles,
    List<String> interests,
    Map<String, Integer> requiredSkillCounts,
    Map<String, Integer> requiredRoleCounts
) {
    public ProjectData(
        Long id,
        String name,
        String projectType,
        int teamSize,
        List<String> requiredSkills,
        List<String> requiredRoles,
        List<String> interests
    ) {
        this(id, name, projectType, teamSize, requiredSkills, requiredRoles, interests, Map.of(), Map.of());
    }

    public ProjectData {
        if (requiredSkills == null) requiredSkills = List.of();
        if (requiredRoles == null) requiredRoles = List.of();
        if (interests == null) interests = List.of();
        if (requiredSkillCounts == null) requiredSkillCounts = Map.of();
        if (requiredRoleCounts == null) requiredRoleCounts = Map.of();
        if (projectType == null || projectType.isBlank()) projectType = "OTHER";
    }
}
