package com.campus.matching;

import com.campus.matching.factory.ExperienceMatcher;
import com.campus.matching.factory.InterestMatcher;
import com.campus.matching.factory.RoleMatcher;
import com.campus.matching.factory.SkillMatcher;
import com.campus.matching.model.*;
import com.campus.matching.service.MatchingProxy;
import com.campus.matching.service.MatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchingEngineTest {

    private MatchingService matchingService;

    @BeforeEach
    public void setUp() {
        matchingService = new MatchingProxy();
    }

    @Test
    public void test1SkillMatching() {
        SkillMatcher matcher = new SkillMatcher();
        ProjectData project = new ProjectData(1L, "Web Proj", "WEB", 2, List.of("Java", "Spring Boot"), List.of(), List.of());
        StudentData student = new StudentData(101L, "Alice", List.of("Java"), List.of(), List.of(), "INTERMEDIATE");

        double matchScore = matcher.score(project, student);
        assertEquals(50.0, matchScore, 0.01);
    }

    @Test
    public void test2RoleMatching() {
        RoleMatcher matcher = new RoleMatcher();
        ProjectData project = new ProjectData(1L, "Web Proj", "WEB", 2, List.of(), List.of("Backend Developer", "Frontend Developer"), List.of());
        StudentData student = new StudentData(101L, "Bob", List.of(), List.of(), List.of("Backend Developer"), "INTERMEDIATE");

        double matchScore = matcher.score(project, student);
        assertEquals(50.0, matchScore, 0.01);
    }

    @Test
    public void test3InterestMatching() {
        InterestMatcher matcher = new InterestMatcher();
        ProjectData project = new ProjectData(1L, "AI Proj", "ML", 2, List.of(), List.of(), List.of("Artificial Intelligence"));
        StudentData student = new StudentData(101L, "Charlie", List.of(), List.of("Artificial Intelligence"), List.of(), "INTERMEDIATE");

        double matchScore = matcher.score(project, student);
        assertEquals(100.0, matchScore, 0.01);
    }

    @Test
    public void test4ExperienceMatching() {
        ExperienceMatcher matcher = new ExperienceMatcher();
        ProjectData project = new ProjectData(1L, "Proj", "WEB", 2, List.of(), List.of(), List.of());
        StudentData studentAdvanced = new StudentData(101L, "Dave", List.of(), List.of(), List.of(), "ADVANCED");
        StudentData studentBeginner = new StudentData(102L, "Eve", List.of(), List.of(), List.of(), "BEGINNER");

        assertEquals(100.0, matcher.score(project, studentAdvanced), 0.01);
        assertEquals(40.0, matcher.score(project, studentBeginner), 0.01);
    }

    @Test
    public void test5IndividualScoreCalculation() {
        ProjectData project = new ProjectData(1L, "Full Stack", "WEB", 2, List.of("Java"), List.of("Backend Developer"), List.of("Web Development"));
        StudentData student = new StudentData(101L, "Frank", List.of("Java"), List.of("Web Development"), List.of("Backend Developer"), "ADVANCED");

        MatchRequest request = new MatchRequest(project, List.of(student));
        MatchResponse response = matchingService.findTeams(request);

        assertNotNull(response);
        assertFalse(response.recommendations().isEmpty());
        MemberRecommendation member = response.recommendations().get(0).members().get(0);
        assertEquals(100.0, member.individualScore(), 0.01);
    }

    @Test
    public void test6TeamScoreCalculation() {
        ProjectData project = new ProjectData(1L, "Team Test", "WEB", 2, List.of("Java", "React"), List.of("Backend Developer", "Frontend Developer"), List.of("Web"));
        StudentData s1 = new StudentData(101L, "Alice", List.of("Java"), List.of("Web"), List.of("Backend Developer"), "ADVANCED");
        StudentData s2 = new StudentData(102L, "Bob", List.of("React"), List.of("Web"), List.of("Frontend Developer"), "INTERMEDIATE");

        MatchRequest request = new MatchRequest(project, List.of(s1, s2));
        MatchResponse response = matchingService.findTeams(request);

        TeamRecommendation topTeam = response.recommendations().get(0);
        assertTrue(topTeam.score() > 0);
    }

    @Test
    public void test7SkillCoverage() {
        ProjectData project = new ProjectData(1L, "Skill Coverage Proj", "WEB", 2, List.of("Java", "Docker"), List.of(), List.of());
        StudentData s1 = new StudentData(101L, "Alice", List.of("Java"), List.of(), List.of(), "INTERMEDIATE");
        StudentData s2 = new StudentData(102L, "Bob", List.of("Docker"), List.of(), List.of(), "INTERMEDIATE");

        MatchRequest request = new MatchRequest(project, List.of(s1, s2));
        MatchResponse response = matchingService.findTeams(request);

        TeamRecommendation team = response.recommendations().get(0);
        assertEquals(100.0, team.skillCoverage(), 0.01);
    }

    @Test
    public void test8RoleCoverage() {
        ProjectData project = new ProjectData(1L, "Role Coverage Proj", "WEB", 2, List.of(), List.of("DevOps", "QA"), List.of());
        StudentData s1 = new StudentData(101L, "Alice", List.of(), List.of(), List.of("DevOps"), "INTERMEDIATE");
        StudentData s2 = new StudentData(102L, "Bob", List.of(), List.of(), List.of("QA"), "INTERMEDIATE");

        MatchRequest request = new MatchRequest(project, List.of(s1, s2));
        MatchResponse response = matchingService.findTeams(request);

        TeamRecommendation team = response.recommendations().get(0);
        assertEquals(100.0, team.roleCoverage(), 0.01);
    }

    @Test
    public void test9TeamGeneration() {
        ProjectData project = new ProjectData(1L, "Gen Proj", "WEB", 2, List.of("Java"), List.of(), List.of());
        StudentData s1 = new StudentData(101L, "Alice", List.of("Java"), List.of(), List.of(), "INTERMEDIATE");
        StudentData s2 = new StudentData(102L, "Bob", List.of("Java"), List.of(), List.of(), "INTERMEDIATE");

        MatchRequest request = new MatchRequest(project, List.of(s1, s2));
        MatchResponse response = matchingService.findTeams(request);

        assertNotNull(response);
        assertFalse(response.recommendations().isEmpty());
        assertEquals(2, response.recommendations().get(0).members().size());
    }

    @Test
    public void test10DuplicatePrevention() {
        ProjectData project = new ProjectData(1L, "Dup Proj", "WEB", 2, List.of("Java"), List.of(), List.of());
        StudentData s1 = new StudentData(101L, "Alice", List.of("Java"), List.of(), List.of(), "INTERMEDIATE");

        MatchRequest request = new MatchRequest(project, List.of(s1, s1));
        MatchResponse response = matchingService.findTeams(request);

        TeamRecommendation team = response.recommendations().get(0);
        assertEquals(1, team.members().size());
    }

    @Test
    public void test11InvalidInput() {
        ProjectData project = new ProjectData(1L, "Null Data Proj", "WEB", 2, null, null, null);
        StudentData s1 = new StudentData(101L, "Alice", null, null, null, "UNKNOWN_EXP");

        MatchRequest request = new MatchRequest(project, List.of(s1));
        MatchResponse response = matchingService.findTeams(request);

        assertNotNull(response);
        assertFalse(response.recommendations().isEmpty());
    }

    @Test
    public void test12ProxyValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            matchingService.findTeams(new MatchRequest(null, List.of()));
        });
    }
}
