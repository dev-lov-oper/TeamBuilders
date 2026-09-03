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

public class InvitationDAOTest {

    private InvitationDAO invitationDAO;
    private ProjectDAO projectDAO;
    private StudentDAO studentDAO;
    private TeamDAO teamDAO;

    private Student sender;
    private Student receiver1;
    private Student receiver2;
    private Project project;

    @BeforeEach
    public void setUp() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        invitationDAO = new InvitationDAOImpl();
        projectDAO = new ProjectDAOImpl();
        studentDAO = new StudentDAOImpl();
        teamDAO = new TeamDAOImpl();

        String suffix = UUID.randomUUID().toString().substring(0, 6);
        sender = studentDAO.createStudent(new Student(null, "sender_" + suffix, "sender_" + suffix + "@campus.edu", "CS", 4, "ADVANCED", "Bio"));
        receiver1 = studentDAO.createStudent(new Student(null, "rec1_" + suffix, "rec1_" + suffix + "@campus.edu", "EE", 2, "INTERMEDIATE", "Bio"));
        receiver2 = studentDAO.createStudent(new Student(null, "rec2_" + suffix, "rec2_" + suffix + "@campus.edu", "ME", 3, "BEGINNER", "Bio"));

        project = projectDAO.createProject(new Project(null, "Invitation Project " + suffix, "Testing invitations", "WEB", 3, "OPEN", sender.getId()));
    }

    @Test
    public void testCreateAndGetInvitation() {
        Invitation inv = new Invitation(null, project.getId(), sender.getId(), receiver1.getId(), "Frontend Developer", "PENDING");
        Invitation created = invitationDAO.createInvitation(inv);

        assertNotNull(created.getId());
        assertEquals("PENDING", created.getStatus());

        Optional<Invitation> fetchedOpt = invitationDAO.getInvitationById(created.getId());
        assertTrue(fetchedOpt.isPresent());

        Invitation fetched = fetchedOpt.get();
        assertEquals("Frontend Developer", fetched.getRoleName());
        assertEquals(receiver1.getUsername(), fetched.getReceiverName());
    }

    @Test
    public void testRule1DuplicatePendingPrevention() {
        Invitation inv1 = new Invitation(null, project.getId(), sender.getId(), receiver1.getId(), "Role A", "PENDING");
        invitationDAO.createInvitation(inv1);

        // Attempting to send a second pending invitation to the same student for the same project
        assertThrows(RuntimeException.class, () -> {
            invitationDAO.createInvitation(new Invitation(null, project.getId(), sender.getId(), receiver1.getId(), "Role B", "PENDING"));
        });
    }

    @Test
    public void testRule2And3InvalidEntityPrevention() {
        Long fakeStudentId = 999999L;
        Long fakeProjectId = 888888L;

        // Invalid Receiver
        assertThrows(RuntimeException.class, () -> {
            invitationDAO.createInvitation(new Invitation(null, project.getId(), sender.getId(), fakeStudentId, "Role", "PENDING"));
        });

        // Invalid Project
        assertThrows(RuntimeException.class, () -> {
            invitationDAO.createInvitation(new Invitation(null, fakeProjectId, sender.getId(), receiver1.getId(), "Role", "PENDING"));
        });
    }

    @Test
    public void testRule4AcceptedInvitationAddsToTeam() {
        Invitation inv = invitationDAO.createInvitation(new Invitation(null, project.getId(), sender.getId(), receiver1.getId(), "Backend Architect", "PENDING"));

        // Accept invitation
        assertTrue(invitationDAO.respondToInvitation(inv.getId(), "ACCEPTED"));

        Optional<Invitation> updatedOpt = invitationDAO.getInvitationById(inv.getId());
        assertTrue(updatedOpt.isPresent());
        assertEquals("ACCEPTED", updatedOpt.get().getStatus());

        // Verify that student was added to project team via JDBC transaction
        Optional<Team> teamOpt = teamDAO.getTeamByProjectId(project.getId());
        assertTrue(teamOpt.isPresent(), "Team should be automatically created upon invitation acceptance");

        Team team = teamOpt.get();
        assertFalse(team.getMembers().isEmpty());
        assertTrue(team.getMembers().stream().anyMatch(m -> m.getStudentId().equals(receiver1.getId())), "Accepted student should be present in project team");
    }

    @Test
    public void testRule5RejectedInvitationDoesNotAddToTeam() {
        Invitation inv = invitationDAO.createInvitation(new Invitation(null, project.getId(), sender.getId(), receiver2.getId(), "UI/UX Designer", "PENDING"));

        // Reject invitation
        assertTrue(invitationDAO.respondToInvitation(inv.getId(), "REJECTED"));

        Optional<Invitation> updatedOpt = invitationDAO.getInvitationById(inv.getId());
        assertTrue(updatedOpt.isPresent());
        assertEquals("REJECTED", updatedOpt.get().getStatus());

        // Verify that student was NOT added to team
        Optional<Team> teamOpt = teamDAO.getTeamByProjectId(project.getId());
        if (teamOpt.isPresent()) {
            assertTrue(teamOpt.get().getMembers().stream().noneMatch(m -> m.getStudentId().equals(receiver2.getId())), "Rejected student should NOT be in project team");
        }
    }

    @Test
    public void testFilterByStudentAndProject() {
        invitationDAO.createInvitation(new Invitation(null, project.getId(), sender.getId(), receiver1.getId(), "Role A", "PENDING"));
        invitationDAO.createInvitation(new Invitation(null, project.getId(), sender.getId(), receiver2.getId(), "Role B", "PENDING"));

        List<Invitation> forStudent = invitationDAO.getInvitationsForStudent(receiver1.getId());
        assertFalse(forStudent.isEmpty());

        List<Invitation> forProject = invitationDAO.getInvitationsForProject(project.getId());
        assertEquals(2, forProject.size());
    }

    @Test
    public void testDeleteInvitation() {
        Invitation inv = invitationDAO.createInvitation(new Invitation(null, project.getId(), sender.getId(), receiver1.getId(), "Role C", "PENDING"));
        Long id = inv.getId();

        assertTrue(invitationDAO.deleteInvitation(id));
        assertTrue(invitationDAO.getInvitationById(id).isEmpty());
    }
}
