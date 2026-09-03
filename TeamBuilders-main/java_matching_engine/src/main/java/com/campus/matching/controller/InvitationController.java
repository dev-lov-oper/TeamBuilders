package com.campus.matching.controller;

import com.campus.matching.dao.InvitationDAO;
import com.campus.matching.model.Invitation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller exposing HTTP endpoints for Invitation management and acceptance workflow.
 * Delegates database persistence tasks to InvitationDAO (DAO pattern).
 */
@RestController
@RequestMapping("/api/invitations")
@CrossOrigin
public class InvitationController {

    private final InvitationDAO invitationDAO;

    public InvitationController(InvitationDAO invitationDAO) {
        this.invitationDAO = invitationDAO;
    }

    /**
     * POST /api/invitations - Send an invitation to a student.
     */
    @PostMapping
    public ResponseEntity<Invitation> createInvitation(@RequestBody Invitation invitation) {
        if (invitation == null || invitation.getProjectId() == null || invitation.getReceiverId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Invitation created = invitationDAO.createInvitation(invitation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/invitations - Retrieve all invitations.
     */
    @GetMapping
    public ResponseEntity<List<Invitation>> getAllInvitations() {
        return ResponseEntity.ok(invitationDAO.getAllInvitations());
    }

    /**
     * GET /api/invitations/{id} - Get invitation by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Invitation> getInvitationById(@PathVariable Long id) {
        Optional<Invitation> invOpt = invitationDAO.getInvitationById(id);
        return invOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/invitations/student/{studentId} - Get invitations for a student.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Invitation>> getInvitationsForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(invitationDAO.getInvitationsForStudent(studentId));
    }

    /**
     * GET /api/invitations/project/{projectId} - Get invitations for a project.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Invitation>> getInvitationsForProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(invitationDAO.getInvitationsForProject(projectId));
    }

    /**
     * PUT /api/invitations/{id}/status?status=ACCEPTED - Respond to an invitation (ACCEPTED or REJECTED).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> respondToInvitation(@PathVariable Long id, @RequestParam String status) {
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        boolean success = invitationDAO.respondToInvitation(id, status);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    /**
     * DELETE /api/invitations/{id} - Delete an invitation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvitation(@PathVariable Long id) {
        boolean deleted = invitationDAO.deleteInvitation(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
