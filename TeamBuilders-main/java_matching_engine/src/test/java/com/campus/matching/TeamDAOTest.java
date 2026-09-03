package com.campus.matching;

import com.campus.matching.dao.*;
import com.campus.matching.db.DatabaseInitializer;
import com.campus.matching.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TeamDAOTest {

    private TeamDAO teamDAO;
    private ProjectDAO projectDAO;
    private StudentDAO studentDAO;

    private Student student1;
    private Student student2;
    private Student student3;
    private Project projectSize2;

    @BeforeEach
    public void setUp() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        teamDAO = new TeamDAOImpl();
        projectDAO = new ProjectDAOImpl();
        studentDAO = new StudentDAOImpl();

        String suffix = UUID.randomUUID().toString().substring(0, 6);
        student1 = studentDAO.createStudent(new Student(null, "s1_" + suffix, "s1_" + suffix + "@campus.edu", "CS", 3, "ADVANCED", "Bio"));
        student2 = studentDAO.createStudent(new Student(null, "s2_" + suffix, "s2_" + suffix + "@campus.edu", "EE", 2, "INTERMEDIATE", "Bio"));
        student3 = studentDAO.createStudent(new Student(null, "s3_" + suffix, "s3_" + suffix + "@campus.edu", "ME", 4, "BEGINNER", "Bio"));

        // Project with max team size = 2
        projectSize2 = projectDAO.createProject(new Project(null, "Team Project " + suffix, "Testing teams", "WEB", 2, "OPEN", student1.getId()));
    }

    @Test
    public void testCreateAndGetTeam() {
        Team team = new Team(projectSize2.getId(), "Option #1", false);
        TeamMember member1 = new TeamMember(null, student1.getId(), null, "Backend Lead");
        team.setMembers(List.of(member1));

        Team created = teamDAO.createTeam(team);
        assertNotNull(created.getId());

        Optional<Team> fetchedOpt = teamDAO.getTeamById(created.getId());
        assertTrue(fetchedOpt.isPresent());

        Team fetched = fetchedOpt.get();
        assertEquals("Option #1", fetched.getName());
        assertEquals(1, fetched.getMembers().size());
        assertEquals("Backend Lead", fetched.getMembers().get(0).getAssignedRoleName());
    }

    @Test
    public void testRule1And3DuplicateStudentPrevention() {
        Team team = teamDAO.createTeam(new Team(projectSize2.getId(), "Option #2", false));
        assertTrue(teamDAO.addMemberToTeam(team.getId(), student1.getId(), null, "Role A"));

        // Attempting to add student1 again should throw IllegalStateException
        assertThrows(RuntimeException.class, () -> {
            teamDAO.addMemberToTeam(team.getId(), student1.getId(), null, "Role B");
        });
    }

    @Test
    public void testRule2TeamSizeLimitEnforcement() {
        // Project size is 2
        Team team = teamDAO.createTeam(new Team(projectSize2.getId(), "Option #3", false));
        assertTrue(teamDAO.addMemberToTeam(team.getId(), student1.getId(), null, "Member 1"));
        assertTrue(teamDAO.addMemberToTeam(team.getId(), student2.getId(), null, "Member 2"));

        // Adding 3rd member to a max-size-2 project team should fail
        assertThrows(RuntimeException.class, () -> {
            teamDAO.addMemberToTeam(team.getId(), student3.getId(), null, "Member 3");
        });
    }

    @Test
    public void testRule4InvalidStudentPrevention() {
        Team team = teamDAO.createTeam(new Team(projectSize2.getId(), "Option #4", false));
        Long fakeStudentId = 999999L;

        assertThrows(RuntimeException.class, () -> {
            teamDAO.addMemberToTeam(team.getId(), fakeStudentId, null, "Role");
        });
    }

    @Test
    public void testRule5InvalidProjectPrevention() {
        Long fakeProjectId = 888888L;
        Team team = new Team(fakeProjectId, "Invalid Team", false);

        assertThrows(RuntimeException.class, () -> {
            teamDAO.createTeam(team);
        });
    }

    @Test
    public void testUpdateAndFinalizeTeam() {
        Team team = teamDAO.createTeam(new Team(projectSize2.getId(), "Initial Name", false));

        team.setName("Finalized Option");
        team.setFinalized(true);

        Team updated = teamDAO.updateTeam(team);
        assertTrue(updated.isFinalized());

        Optional<Project> projOpt = projectDAO.getProjectById(projectSize2.getId());
        assertTrue(projOpt.isPresent());
        assertEquals("CLOSED", projOpt.get().getStatus(), "Finalizing team should change project status to CLOSED");
    }

    @Test
    public void testDeleteTeam() {
        Team team = teamDAO.createTeam(new Team(projectSize2.getId(), "To Delete", false));
        Long teamId = team.getId();

        assertTrue(teamDAO.deleteTeam(teamId));
        assertTrue(teamDAO.getTeamById(teamId).isEmpty());
    }
}
