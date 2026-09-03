package com.campus.matching.model;

public record MemberRecommendation(
    Long studentId,
    String name,
    String role,
    double individualScore,
    double skillScore,
    double roleScore,
    double interestScore,
    double experienceScore
) {}
