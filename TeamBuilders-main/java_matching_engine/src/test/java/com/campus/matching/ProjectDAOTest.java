package com.campus.matching;

import com.campus.matching.dao.*;
import com.campus.matching.db.DatabaseInitializer;
import com.campus.matching.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectDAOTest {

    private ProjectDAO projectDAO;
    private StudentDAO studentDAO;
    private SkillDAO skillDAO;
    private RoleDAO roleDAO;
    private InterestDAO interestDAO;

    private Student owner;

    @BeforeEach
    public void setUp() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        projectDAO = new ProjectDAOImpl();
        studentDAO = new StudentDAOImpl();
        skillDAO = new SkillDAOImpl();
        roleDAO = new RoleDAOImpl();
        interestDAO = new InterestDAOImpl();

        String user = "owner_" + UUID.randomUUID().toString().substring(0, 6);
        owner = studentDAO.createStudent(new Student(null, user, user + "@campus.edu", "CS", 4, "ADVANCED", "Owner"));
    }

    @Test
    public void testCreateAndGetProject() {
        String projName = "Smart Campus IoT_" + UUID.randomUUID().toString().substring(0, 6);
        Project project = new Project(null, projName, "IoT sensor monitoring", "HARDWARE", 4, "OPEN", owner.getId());
        project.setRequiredSkills(List.of(new Skill("C++"), new Skill("Raspberry Pi")));
        project.setRequiredSkillCounts(Map.of("C++", 2, "Raspberry Pi", 1));
        project.setRequiredRoles(List.of(new Role("Embedded Dev")));
        project.setInterests(List.of(new Interest("IoT")));

        Project created = projectDAO.createProject(project);
        assertNotNull(created.getId(), "Created project should have an ID");

        Optional<Project> fetchedOpt = projectDAO.getProjectById(created.getId());
        assertTrue(fetchedOpt.isPresent());

        Project fetched = fetchedOpt.get();
        assertEquals(projName, fetched.getName());
        assertEquals("HARDWARE", fetched.getProjectType());
        assertEquals(4, fetched.getTeamSize());
        assertFalse(fetched.getRequiredSkills().isEmpty());
        assertEquals(2, fetched.getRequiredSkillCounts().get("C++"));
        assertFalse(fetched.getRequiredRoles().isEmpty());
        assertFalse(fetched.getInterests().isEmpty());
    }

    @Test
    public void testGetAllProjects() {
        String projName = "AI Vision_" + UUID.randomUUID().toString().substring(0, 6);
        projectDAO.createProject(new Project(null, projName, "Computer vision model", "ML", 3, "OPEN", owner.getId()));

        List<Project> projects = projectDAO.getAllProjects();
        assertNotNull(projects);
        assertFalse(projects.isEmpty());
    }

    @Test
    public void testUpdateProject() {
        String projName = "Web App_" + UUID.randomUUID().toString().substring(0, 6);
        Project project = projectDAO.createProject(new Project(null, projName, "Initial desc", "WEB", 3, "OPEN", owner.getId()));

        project.setDescription("Updated desc");
        project.setTeamSize(5);
        project.setStatus("CLOSED");
        project.setRequiredSkills(List.of(new Skill("Java"), new Skill("Docker")));

        Project updated = projectDAO.updateProject(project);
        assertEquals("Updated desc", updated.getDescription());
        assertEquals(5, updated.getTeamSize());
        assertEquals("CLOSED", updated.getStatus());

        Optional<Project> refetchedOpt = projectDAO.getProjectById(project.getId());
        assertTrue(refetchedOpt.isPresent());
        assertEquals("CLOSED", refetchedOpt.get().getStatus());
    }

    @Test
    public void testDeleteProject() {
        String projName = "ToDelete_" + UUID.randomUUID().toString().substring(0, 6);
        Project project = projectDAO.createProject(new Project(null, projName, "Temporary proj", "OTHER", 2, "OPEN", owner.getId()));

        Long projId = project.getId();
        assertTrue(projectDAO.deleteProject(projId));
        assertTrue(projectDAO.getProjectById(projId).isEmpty());
    }

    @Test
    public void testProjectRequirements() {
        String projName = "ReqTest_" + UUID.randomUUID().toString().substring(0, 6);
        Project project = projectDAO.createProject(new Project(null, projName, "Req test desc", "WEB", 3, "OPEN", owner.getId()));

        Skill skill = skillDAO.createSkill(new Skill("K8s_" + UUID.randomUUID().toString().substring(0, 4)));
        Role role = roleDAO.createRole(new Role("DevOps_" + UUID.randomUUID().toString().substring(0, 4)));
        Interest interest = interestDAO.createInterest(new Interest("Cloud_" + UUID.randomUUID().toString().substring(0, 4)));

        assertTrue(projectDAO.addProjectSkill(project.getId(), skill.getId(), 2));
        assertTrue(projectDAO.addProjectRole(project.getId(), role.getId(), 1));
        assertTrue(projectDAO.addProjectInterest(project.getId(), interest.getId()));

        List<Skill> skills = projectDAO.getProjectSkills(project.getId());
        assertTrue(skills.stream().anyMatch(s -> s.getId().equals(skill.getId())));

        List<Role> roles = projectDAO.getProjectRoles(project.getId());
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals(role.getId())));

        List<Interest> interests = projectDAO.getProjectInterests(project.getId());
        assertTrue(interests.stream().anyMatch(i -> i.getId().equals(interest.getId())));

        assertTrue(projectDAO.removeProjectSkill(project.getId(), skill.getId()));
        assertTrue(projectDAO.removeProjectRole(project.getId(), role.getId()));
        assertTrue(projectDAO.removeProjectInterest(project.getId(), interest.getId()));

        assertTrue(projectDAO.getProjectSkills(project.getId()).stream().noneMatch(s -> s.getId().equals(skill.getId())));
    }
}
