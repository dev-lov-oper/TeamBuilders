package com.campus.matching.controller;

import com.campus.matching.model.*;
import com.campus.matching.service.*;
import org.springframework.web.bind.annotation.*;

/**
 * REST entry point. Note it talks only to the MatchingService
 * interface (PROXY pattern) - specifically to a MatchingProxy,
 * which validates the request before handing it to the real
 * matching logic in RealMatchingService.
 */
@RestController
@RequestMapping("/api/matching")
@CrossOrigin
public class MatchingController {

    private final MatchingService service = new MatchingProxy();

    private final com.campus.matching.db.DatabaseInitializer dbInitializer;

    public MatchingController(com.campus.matching.db.DatabaseInitializer dbInitializer) {
        this.dbInitializer = dbInitializer;
    }

    @PostMapping("/find-teams")
    public MatchResponse findTeams(@RequestBody MatchRequest request) {
        return service.findTeams(request);
    }

    @GetMapping("/find-teams/{projectId}")
    public MatchResponse findTeamsByProjectId(@PathVariable Long projectId) {
        ProjectData projectPlaceholder = new ProjectData(projectId, "Project " + projectId, "WEB", 3, java.util.List.of(), java.util.List.of(), java.util.List.of());
        MatchRequest request = new MatchRequest(projectPlaceholder, java.util.List.of());
        return service.findTeams(request);
    }

    @GetMapping("/db-status")
    public java.util.Map<String, Object> checkDbStatus() {
        com.campus.matching.db.DatabaseManager db1 = com.campus.matching.db.DatabaseManager.getInstance();
        com.campus.matching.db.DatabaseManager db2 = com.campus.matching.db.DatabaseManager.getInstance();

        boolean isSingleton = (db1 == db2);
        boolean isConnected = db1.testConnection();
        java.util.List<String> tables = dbInitializer.getExistingTables();

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("status", isConnected ? "UP" : "DOWN");
        result.put("connectionValid", isConnected);
        result.put("dbUrl", db1.getDbUrl());
        result.put("isSingletonValid", isSingleton);
        result.put("tableCount", tables.size());
        result.put("tables", tables);
        return result;
    }
}
