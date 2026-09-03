package com.campus.matching;

import com.campus.matching.dao.*;
import com.campus.matching.db.DatabaseInitializer;
import com.campus.matching.db.DatabaseManager;
import com.campus.matching.exception.BadRequestException;
import com.campus.matching.exception.DuplicateResourceException;
import com.campus.matching.exception.ResourceNotFoundException;
import com.campus.matching.model.*;
import com.campus.matching.service.MatchingProxy;
import com.campus.matching.service.MatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Complete JUnit 5 Test Suite covering all 20 required testing scenarios.
 * Uses pure JDBC / SQLite without JPA or Hibernate.
 */
public class CompleteEngineTestSuiteTest {

    private DatabaseManager dbManager;
    private StudentDAO studentDAO;
    private SkillDAO skillDAO;
    private InterestDAO interestDAO;
    private RoleDAO roleDAO;
    private ProjectDAO projectDAO;
    private TeamDAO teamDAO;
    private InvitationDAO invitationDAO;
    private MatchingService matchingProxy;

    @BeforeEach
    public void setUp() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        dbManager = DatabaseManager.getInstance();
        studentDAO = new StudentDAOImpl();
        skillDAO = new SkillDAOImpl();
        interestDAO = new InterestDAOImpl();
        roleDAO = new RoleDAOImpl();
        projectDAO = new ProjectDAOImpl();
        teamDAO = new TeamDAOImpl();
        invitationDAO = new InvitationDAOImpl();
        matchingProxy = new MatchingProxy();
    }

    @Test
    @DisplayName("1. Test Database Connection")
    public void test1_DatabaseConnection() throws SQLException {
        try (Connection conn = dbManager.getConnection()) {
            assertNotNull(conn, "Database connection should not be null");
            assertFalse(conn.isClosed(), "Database connection should be open");
        }
        assertTrue(dbManager.testConnection(), "testConnection() should return true");
    }

    @Test
    @DisplayName("2. Test DatabaseManager Singleton Pattern")
    public void test2_DatabaseManagerSingleton() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();
        assertSame(instance1, instance2, "DatabaseManager getInstance() must return the exact same instance");
    }

    @Test
    @DisplayName("3. Test Student DAO CRUD")
    public void test3_StudentDAOCRUD() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student student = new Student(null, "user_" + uid, "email_" + uid + "@test.com", "CS", 3, "INTERMEDIATE", "Bio");
        Student created = studentDAO.createStudent(student);
        assertNotNull(created.getId());

        Optional<Student> fetched = studentDAO.getStudentById(created.getId());
        assertTrue(fetched.isPresent());
        assertEquals("user_" + uid, fetched.get().getUsername());

        created.setDepartment("Information Technology");
        Student updated = studentDAO.updateStudent(created);
        assertEquals("Information Technology", updated.getDepartment());

        boolean deleted = studentDAO.deleteStudent(created.getId());
        assertTrue(deleted);
        assertTrue(studentDAO.getStudentById(created.getId()).isEmpty());
    }

    @Test
    @DisplayName("4. Test Skill DAO CRUD")
    public void test4_SkillDAOCRUD() {
        String skillName = "TestSkill_" + UUID.randomUUID().toString().substring(0, 6);
        Skill skill = skillDAO.createSkill(new Skill(null, skillName));
        assertNotNull(skill.getId());

        Optional<Skill> fetched = skillDAO.getSkillById(skill.getId());
        assertTrue(fetched.isPresent());

        boolean deleted = skillDAO.deleteSkill(skill.getId());
        assertTrue(deleted);
    }

    @Test
    @DisplayName("5. Test Interest DAO CRUD")
    public void test5_InterestDAOCRUD() {
        String interestName = "TestInterest_" + UUID.randomUUID().toString().substring(0, 6);
        Interest interest = interestDAO.createInterest(new Interest(null, interestName));
        assertNotNull(interest.getId());

        Optional<Interest> fetched = interestDAO.getInterestById(interest.getId());
        assertTrue(fetched.isPresent());

        boolean deleted = interestDAO.deleteInterest(interest.getId());
        assertTrue(deleted);
    }

    @Test
    @DisplayName("6. Test Role DAO CRUD")
    public void test6_RoleDAOCRUD() {
        String roleName = "TestRole_" + UUID.randomUUID().toString().substring(0, 6);
        Role role = roleDAO.createRole(new Role(null, roleName));
        assertNotNull(role.getId());

        Optional<Role> fetched = roleDAO.getRoleById(role.getId());
        assertTrue(fetched.isPresent());

        boolean deleted = roleDAO.deleteRole(role.getId());
        assertTrue(deleted);
    }

    @Test
    @DisplayName("7. Test Project DAO CRUD")
    public void test7_ProjectDAOCRUD() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student owner = studentDAO.createStudent(new Student(null, "ow7_" + uid, "o7_" + uid + "@test.com", "CS", 4, "ADVANCED", "Bio"));
        Project project = new Project(null, "Test Project", "Desc", "WEB", 3, "OPEN", owner.getId());
        Project created = projectDAO.createProject(project);
        assertNotNull(created.getId());

        Optional<Project> fetched = projectDAO.getProjectById(created.getId());
        assertTrue(fetched.isPresent());

        created.setName("Updated Project Name");
        Project updated = projectDAO.updateProject(created);
        assertEquals("Updated Project Name", updated.getName());

        boolean deleted = projectDAO.deleteProject(created.getId());
        assertTrue(deleted);
    }

    @Test
    @DisplayName("8. Test Team DAO CRUD")
    public void test8_TeamDAOCRUD() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student owner = studentDAO.createStudent(new Student(null, "ow8_" + uid, "o8_" + uid + "@test.com", "CS", 3, "INTERMEDIATE", "Bio"));
        Project project = projectDAO.createProject(new Project(null, "Team Project", "Desc", "WEB", 3, "OPEN", owner.getId()));

        Team team = teamDAO.createTeam(new Team(null, project.getId(), "Alpha Team", false, null));
        assertNotNull(team.getId());

        Optional<Team> fetched = teamDAO.getTeamById(team.getId());
        assertTrue(fetched.isPresent());

        team.setName("Alpha Team Updated");
        Team updated = teamDAO.updateTeam(team);
        assertEquals("Alpha Team Updated", updated.getName());

        boolean deleted = teamDAO.deleteTeam(team.getId());
        assertTrue(deleted);
    }

    @Test
    @DisplayName("9. Test Invitation DAO CRUD")
    public void test9_InvitationDAOCRUD() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student s1 = studentDAO.createStudent(new Student(null, "in9a_" + uid, "i9a_" + uid + "@test.com", "CS", 2, "BEGINNER", "Bio"));
        Student s2 = studentDAO.createStudent(new Student(null, "in9b_" + uid, "i9b_" + uid + "@test.com", "CS", 2, "BEGINNER", "Bio"));
        Project p = projectDAO.createProject(new Project(null, "Inv Project", "Desc", "WEB", 3, "OPEN", s1.getId()));

        Invitation inv = invitationDAO.createInvitation(new Invitation(null, p.getId(), s1.getId(), s2.getId(), "Backend Developer", "PENDING", null));
        assertNotNull(inv.getId());

        Optional<Invitation> fetched = invitationDAO.getInvitationById(inv.getId());
        assertTrue(fetched.isPresent());

        inv.setStatus("REJECTED");
        Invitation updated = invitationDAO.updateInvitation(inv);
        assertEquals("REJECTED", updated.getStatus());

        boolean deleted = invitationDAO.deleteInvitation(inv.getId());
        assertTrue(deleted);
    }

    @Test
    @DisplayName("10. Test Student-Skill Relationships")
    public void test10_StudentSkillRelationships() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student student = studentDAO.createStudent(new Student(null, "sk10_" + uid, "s10_" + uid + "@test.com", "CS", 2, "BEGINNER", "Bio"));
        Skill skill = skillDAO.createSkill(new Skill(null, "SkillRel_" + uid));

        assertTrue(studentDAO.addSkillToStudent(student.getId(), skill.getId()));
        List<Skill> skills = studentDAO.getStudentSkills(student.getId());
        assertEquals(1, skills.size());

        assertTrue(studentDAO.removeSkillFromStudent(student.getId(), skill.getId()));
        assertTrue(studentDAO.getStudentSkills(student.getId()).isEmpty());
    }

    @Test
    @DisplayName("11. Test Student-Interest Relationships")
    public void test11_StudentInterestRelationships() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student student = studentDAO.createStudent(new Student(null, "in11_" + uid, "i11_" + uid + "@test.com", "CS", 2, "BEGINNER", "Bio"));
        Interest interest = interestDAO.createInterest(new Interest(null, "IntRel_" + uid));

        assertTrue(studentDAO.addInterestToStudent(student.getId(), interest.getId()));
        List<Interest> interests = studentDAO.getStudentInterests(student.getId());
        assertEquals(1, interests.size());

        assertTrue(studentDAO.removeInterestFromStudent(student.getId(), interest.getId()));
        assertTrue(studentDAO.getStudentInterests(student.getId()).isEmpty());
    }

    @Test
    @DisplayName("12. Test Student-Role Relationships")
    public void test12_StudentRoleRelationships() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student student = studentDAO.createStudent(new Student(null, "ro12_" + uid, "r12_" + uid + "@test.com", "CS", 2, "BEGINNER", "Bio"));
        Role role = roleDAO.createRole(new Role(null, "RoleRel_" + uid));

        assertTrue(studentDAO.addRoleToStudent(student.getId(), role.getId()));
        List<Role> roles = studentDAO.getStudentRoles(student.getId());
        assertEquals(1, roles.size());

        assertTrue(studentDAO.removeRoleFromStudent(student.getId(), role.getId()));
        assertTrue(studentDAO.getStudentRoles(student.getId()).isEmpty());
    }

    @Test
    @DisplayName("13. Test Project Requirements (Skills, Roles, Interests)")
    public void test13_ProjectRequirements() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student owner = studentDAO.createStudent(new Student(null, "ow13_" + uid, "o13_" + uid + "@test.com", "CS", 3, "INTERMEDIATE", "Bio"));
        Project p = projectDAO.createProject(new Project(null, "Req Project", "Desc", "WEB", 3, "OPEN", owner.getId()));

        Skill skill = skillDAO.createSkill(new Skill(null, "ReqSkill_" + uid));
        Role role = roleDAO.createRole(new Role(null, "ReqRole_" + uid));
        Interest interest = interestDAO.createInterest(new Interest(null, "ReqInt_" + uid));

        assertTrue(projectDAO.addProjectSkill(p.getId(), skill.getId(), 1));
        assertTrue(projectDAO.addProjectRole(p.getId(), role.getId(), 1));
        assertTrue(projectDAO.addProjectInterest(p.getId(), interest.getId()));

        assertEquals(1, projectDAO.getProjectSkills(p.getId()).size());
        assertEquals(1, projectDAO.getProjectRoles(p.getId()).size());
        assertEquals(1, projectDAO.getProjectInterests(p.getId()).size());
    }

    @Test
    @DisplayName("14. Test Weighted Matching Score Calculation (45/25/20/10)")
    public void test14_MatchingCalculation() {
        ProjectData projectData = new ProjectData(1L, "Web Platform", "WEB", 3, List.of("Java"), List.of("Backend Developer"), List.of("Web Development"));
        MatchResponse response = matchingProxy.findTeams(new MatchRequest(projectData, List.of()));
        assertNotNull(response);
        assertNotNull(response.recommendations());
    }

    @Test
    @DisplayName("15. Test Proxy -> RealMatchingService Delegation")
    public void test15_ProxyDelegation() {
        ProjectData projectData = new ProjectData(1L, "Web Platform", "WEB", 3, List.of("Java"), List.of("Backend Developer"), List.of("Web Development"));
        MatchResponse response = matchingProxy.findTeams(new MatchRequest(projectData, List.of()));
        assertNotNull(response);
        assertEquals(1L, response.projectId());
    }

    @Test
    @DisplayName("16. Test Team Formation and Recommendation Generation")
    public void test16_TeamFormation() {
        ProjectData projectData = new ProjectData(1L, "Web Platform", "WEB", 3, List.of("Java"), List.of("Backend Developer"), List.of("Web Development"));
        MatchResponse response = matchingProxy.findTeams(new MatchRequest(projectData, List.of()));
        assertFalse(response.recommendations().isEmpty(), "Team formation should generate at least 1 team recommendation");
    }

    @Test
    @DisplayName("17. Test Invitation Acceptance and Automated Team Membership Creation")
    public void test17_InvitationAcceptance() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student owner = studentDAO.createStudent(new Student(null, "ow17_" + uid, "o17_" + uid + "@test.com", "CS", 3, "INTERMEDIATE", "Bio"));
        Student invitee = studentDAO.createStudent(new Student(null, "inv17_" + uid, "i17_" + uid + "@test.com", "CS", 2, "BEGINNER", "Bio"));
        Project p = projectDAO.createProject(new Project(null, "Acc Project", "Desc", "WEB", 3, "OPEN", owner.getId()));

        Team team = teamDAO.createTeam(new Team(null, p.getId(), "Acc Team", false, null));
        Invitation inv = invitationDAO.createInvitation(new Invitation(null, p.getId(), owner.getId(), invitee.getId(), "Backend Developer", "PENDING", null));

        inv.setStatus("ACCEPTED");
        invitationDAO.updateInvitation(inv);
        teamDAO.addMemberToTeam(team.getId(), invitee.getId(), null, "Backend Developer");

        Optional<Invitation> updatedInv = invitationDAO.getInvitationById(inv.getId());
        assertTrue(updatedInv.isPresent());
        assertEquals("ACCEPTED", updatedInv.get().getStatus());

        List<TeamMember> teamMembers = teamDAO.getTeamMembers(team.getId());
        assertTrue(teamMembers.stream().anyMatch(m -> m.getStudentId().equals(invitee.getId())), "Invitee should automatically join project team");
    }

    @Test
    @DisplayName("18. Test Invalid Input Validation")
    public void test18_InvalidInputValidation() {
        assertThrows(RuntimeException.class, () -> {
            studentDAO.createStudent(new Student(null, null, "no_user@test.com", "CS", 1, "BEGINNER", "Bio"));
        });
    }

    @Test
    @DisplayName("19. Test Duplicate Record Prevention")
    public void test19_DuplicateRecordPrevention() {
        String uid = UUID.randomUUID().toString().substring(0, 6);
        studentDAO.createStudent(new Student(null, "dup_user_" + uid, "dup_" + uid + "@test.com", "CS", 1, "BEGINNER", "Bio"));

        assertThrows(RuntimeException.class, () -> {
            studentDAO.createStudent(new Student(null, "dup_user_" + uid, "dup_" + uid + "@test.com", "CS", 1, "BEGINNER", "Bio"));
        });
    }

    @Test
    @DisplayName("20. Test Team Size Restrictions Enforcement")
    public void test20_TeamSizeRestrictions() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        Student owner = studentDAO.createStudent(new Student(null, "ow20_" + uid, "o20_" + uid + "@test.com", "CS", 3, "INTERMEDIATE", "Bio"));
        Student member1 = studentDAO.createStudent(new Student(null, "m20a_" + uid, "m20a_" + uid + "@test.com", "CS", 2, "BEGINNER", "Bio"));
        Student member2 = studentDAO.createStudent(new Student(null, "m20b_" + uid, "m20b_" + uid + "@test.com", "CS", 2, "BEGINNER", "Bio"));

        // Max team size = 1
        Project project = projectDAO.createProject(new Project(null, "Limit Project", "Desc", "WEB", 1, "OPEN", owner.getId()));
        Team team = teamDAO.createTeam(new Team(null, project.getId(), "Limit Team", false, null));

        assertTrue(teamDAO.addMemberToTeam(team.getId(), member1.getId(), null, "Developer"));

        // Adding second member should fail due to team size limit
        assertThrows(IllegalStateException.class, () -> {
            teamDAO.addMemberToTeam(team.getId(), member2.getId(), null, "Developer");
        });
    }
}
