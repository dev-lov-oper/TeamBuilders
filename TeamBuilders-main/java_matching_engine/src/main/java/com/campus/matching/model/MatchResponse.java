package com.campus.matching.model;
import java.util.List;
public record MatchResponse(Long projectId, List<TeamRecommendation> recommendations) {}
