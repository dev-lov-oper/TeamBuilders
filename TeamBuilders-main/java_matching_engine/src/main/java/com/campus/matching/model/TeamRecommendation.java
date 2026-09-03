package com.campus.matching.model;

import java.util.List;

public record TeamRecommendation(
    double score,
    double skillCoverage,
    double roleCoverage,
    double interestCoverage,
    List<MemberRecommendation> members
) {}
