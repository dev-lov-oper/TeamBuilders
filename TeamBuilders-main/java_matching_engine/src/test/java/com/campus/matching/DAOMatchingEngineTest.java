package com.campus.matching;

import com.campus.matching.dao.*;
import com.campus.matching.db.DatabaseInitializer;
import com.campus.matching.model.*;
import com.campus.matching.service.MatchingProxy;
import com.campus.matching.service.MatchingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DAOMatchingEngineTest {

    private ProjectDAO projectDAO;
    private StudentDAO studentDAO;
    private SkillDAO skillDAO;
    private RoleDAO roleDAO;
    private InterestDAO interestDAO;
    private MatchingService matchingService;

    @BeforeEach
    public void setUp() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        projectDAO = new ProjectDAOImpl();
        studentDAO = new StudentDAOImpl();
        skillDAO = new SkillDAOImpl();
        roleDAO = new RoleDAOImpl();
        interestDAO = new InterestDAOImpl();
        matchingService = new MatchingProxy();
    }

    @Test
    public void testMatchingEngineWithDAOLoadedData() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);

        // Create skills, roles, interests in DB
        Skill javaSkill = skillDAO.createSkill(new Skill("Java_" + suffix));
        Skill springSkill = skillDAO.createSkill(new Skill("Spring_" + suffix));
        Role backendRole = roleDAO.createRole(new Role("Backend_" + suffix));
        Interest aiInterest = interestDAO.createInterest(new Interest("AI_" + suffix));

        // Create Students in SQLite DB
        Student s1 = new Student(null, "alice_" + suffix, "alice_" + suffix + "@campus.edu", "CS", 4, "ADVANCED", "Alice Bio");
        s1.setSkills(List.of(javaSkill, springSkill));
        s1.setPreferredRoles(List.of(backendRole));
        s1.setInterests(List.of(aiInterest));
        s1 = studentDAO.createStudent(s1);

        Student s2 = new Student(null, "bob_" + suffix, "bob_" + suffix + "@campus.edu", "CS", 2, "BEGINNER", "Bob Bio");
        s2.setSkills(List.of(javaSkill));
        s2 = studentDAO.createStudent(s2);

        // Create Project in SQLite DB
        Project project = new Project(null, "DAO Matching Project " + suffix, "DAO test", "WEB", 2, "OPEN", s1.getId());
        project.setRequiredSkills(List.of(javaSkill, springSkill));
        project.setRequiredSkillCounts(Map.of(javaSkill.getName(), 1, springSkill.getName(), 1));
        project.setRequiredRoles(List.of(backendRole));
        project.setInterests(List.of(aiInterest));
        project = projectDAO.createProject(project);

        // Trigger matching via Proxy pattern with only project ID (null candidate list -> auto-loaded from StudentDAO)
        ProjectData projectPlaceholder = new ProjectData(project.getId(), project.getName(), project.getProjectType(), project.getTeamSize(), List.of(), List.of(), List.of());
        MatchRequest request = new MatchRequest(projectPlaceholder, List.of());

        MatchResponse response = matchingService.findTeams(request);

        assertNotNull(response);
        assertEquals(project.getId(), response.projectId());
        assertFalse(response.recommendations().isEmpty(), "Matching engine should generate team recommendations from DAO data");

        TeamRecommendation topTeam = response.recommendations().get(0);
        assertTrue(topTeam.score() > 0, "Team score should be positive");
        assertFalse(topTeam.members().isEmpty(), "Team members should be populated");
    }
}
