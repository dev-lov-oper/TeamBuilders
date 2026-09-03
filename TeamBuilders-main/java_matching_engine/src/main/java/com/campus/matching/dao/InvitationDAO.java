package com.campus.matching.dao;

import com.campus.matching.model.Invitation;

import java.util.List;
import java.util.Optional;

/**
 * ================================
 *  DESIGN PATTERN: DATA ACCESS OBJECT (DAO)
 * ================================
 * Interface defining CRUD, filtering, and transaction-driven status update operations for Invitations.
 */
public interface InvitationDAO {

    Invitation createInvitation(Invitation invitation);

    Optional<Invitation> getInvitationById(Long id);

    List<Invitation> getAllInvitations();

    List<Invitation> getInvitationsForStudent(Long studentId);

    List<Invitation> getInvitationsForProject(Long projectId);

    Invitation updateInvitation(Invitation invitation);

    boolean respondToInvitation(Long invitationId, String status);

    boolean deleteInvitation(Long id);
}
