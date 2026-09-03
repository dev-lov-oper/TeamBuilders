package com.campus.matching.service;

import com.campus.matching.model.MatchRequest;
import com.campus.matching.model.MatchResponse;

import java.util.List;

/**
 * ================================
 *  DESIGN PATTERN: PROXY
 * ================================
 * This is the "Proxy" - it stands in front of RealMatchingService
 * and controls access to it.
 *
 * Before letting a request reach the real (expensive) matching
 * logic, the proxy does cheap, quick checks first:
 *   - Is the request valid at all?
 *   - Are there even any students to consider?
 *
 * Only if those checks pass does it forward the call to the real
 * service. This keeps validation logic out of RealMatchingService,
 * which can then focus purely on the matching algorithm.
 */
public class MatchingProxy implements MatchingService {

    private final MatchingService realService = new RealMatchingService();
    private final com.campus.matching.dao.StudentDAO studentDAO = new com.campus.matching.dao.StudentDAOImpl();

    @Override
    public MatchResponse findTeams(MatchRequest request) {
        if (request == null || request.project() == null) {
            throw new IllegalArgumentException("Invalid matching request: project data is required.");
        }

        Long projectId = request.project().id();

        // Edge case: no candidate students in request AND no candidate students in SQLite DB
        if ((request.students() == null || request.students().isEmpty()) && studentDAO.getAllStudents().isEmpty()) {
            return new MatchResponse(projectId, List.of());
        }

        // Forward to RealMatchingService (Real Subject)
        return realService.findTeams(request);
    }
}
