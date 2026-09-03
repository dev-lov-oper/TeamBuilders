package com.campus.matching.service;

import com.campus.matching.model.MatchRequest;
import com.campus.matching.model.MatchResponse;

/**
 * ================================
 *  DESIGN PATTERN: PROXY
 * ================================
 * This is the common "Subject" interface shared by:
 *   - RealMatchingService  (does the real, heavy work)
 *   - MatchingProxy        (stands in front of the real service)
 *
 * Because both implement the same interface, the Controller can
 * talk to "a MatchingService" without knowing (or caring) whether
 * it's talking to the real thing or the proxy guarding it.
 */
public interface MatchingService {
    MatchResponse findTeams(MatchRequest request);
}
