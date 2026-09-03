package com.campus.matching.model;
import java.util.List;
public record MatchRequest(ProjectData project, List<StudentData> students) {}
