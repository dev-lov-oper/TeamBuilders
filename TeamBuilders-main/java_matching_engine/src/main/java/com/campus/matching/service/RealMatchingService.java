package com.campus.matching.service;

import com.campus.matching.abstractfactory.ProjectTypeFactoryProvider;
import com.campus.matching.abstractfactory.RequirementBundle;
import com.campus.matching.bridge.StudentRoleBridge;
import com.campus.matching.factory.Matcher;
import com.campus.matching.factory.MatcherFactory;
import com.campus.matching.model.*;
import com.campus.matching.singleton.MatchingConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the "Real Subject" behind the Proxy pattern (see MatchingProxy).
 * It does the actual team-matching work, and it also ties together
 * every other pattern in this project:
 *
 *   - FACTORY METHOD  -> gets its four Matcher objects from MatcherFactory
 *   - ABSTRACT FACTORY -> gets typical role hints via ProjectTypeFactoryProvider
 *   - SINGLETON        -> reads the shared scoring weights from MatchingConfiguration
 *   - BRIDGE           -> uses StudentRoleBridge to decide each member's role
 *
 * Overall steps to build team recommendations:
 *   1. Score every student individually and keep the best candidates.
 *   2. Try every possible team (combination of candidates) of the
 *      required team size.
 *   3. Score each possible team as a whole.
 *   4. Return the top 3 best-scoring teams.
 */
public class RealMatchingService implements MatchingService {

    // FACTORY METHOD: ask the factory for each matcher instead of "new"-ing them directly.
    private final Matcher skillMatcher = MatcherFactory.create("SKILL");
    private final Matcher roleMatcher = MatcherFactory.create("ROLE");
    private final Matcher interestMatcher = MatcherFactory.create("INTEREST");
    private final Matcher experienceMatcher = MatcherFactory.create("EXPERIENCE");

    // SINGLETON: always the same shared configuration object.
    private final MatchingConfiguration config = MatchingConfiguration.getInstance();

    // DAO LAYER: Access SQLite database for Projects and Students
    private final com.campus.matching.dao.ProjectDAO projectDAO = new com.campus.matching.dao.ProjectDAOImpl();
    private final com.campus.matching.dao.StudentDAO studentDAO = new com.campus.matching.dao.StudentDAOImpl();

    @Override
    public MatchResponse findTeams(MatchRequest req) {
        ProjectData project = req.project();
        List<StudentData> students = req.students();

        // 1. Load project requirements from SQLite ProjectDAO if ID is provided and requirements are absent
        if (project != null && project.id() != null) {
            boolean missingRequirements = (project.requiredSkills() == null || project.requiredSkills().isEmpty())
                    && (project.requiredRoles() == null || project.requiredRoles().isEmpty())
                    && (project.interests() == null || project.interests().isEmpty());
            if (missingRequirements) {
                java.util.Optional<com.campus.matching.model.Project> dbProjectOpt = projectDAO.getProjectById(project.id());
                if (dbProjectOpt.isPresent()) {
                    project = dbProjectOpt.get().toProjectData();
                }
            }
        }

        // 2. Load candidate students from SQLite StudentDAO if none supplied in request
        if (students == null || students.isEmpty()) {
            List<com.campus.matching.model.Student> dbStudents = studentDAO.getAllStudents();
            students = dbStudents.stream()
                    .map(com.campus.matching.model.Student::toStudentData)
                    .collect(java.util.stream.Collectors.toList());
        }

        if (project == null || students.isEmpty()) {
            return new MatchResponse(project != null ? project.id() : null, List.of());
        }

        // Step 1: score every student and pick the strongest candidates.
        List<ScoredStudent> candidates = rankCandidates(project, students);

        // Step 2: decide how big each team should be.
        int targetTeamSize = Math.max(1, Math.min(
            project.teamSize() > 0 ? project.teamSize() : 3,
            candidates.size()
        ));

        // Step 3: build every possible team of that size and score it.
        List<TeamRecommendation> allTeams = new ArrayList<>();
        generateCombinations(candidates, 0, targetTeamSize, new ArrayList<>(), project, allTeams);

        // Step 4: sort teams best-first and keep only the top 3.
        allTeams.sort((a, b) -> Double.compare(b.score(), a.score()));

        List<TeamRecommendation> topRecommendations = new ArrayList<>();
        for (int i = 0; i < allTeams.size() && i < 3; i++) {
            topRecommendations.add(allTeams.get(i));
        }

        return new MatchResponse(project.id(), topRecommendations);
    }

    /**
     * Scores every student, removes duplicate student IDs (keeping the
     * first occurrence), sorts best-first, and keeps at most 12 of them
     * so the combination search below doesn't explode in size.
     */
    /**
     * Scores every student and selects a diverse candidate pool that guarantees representation
     * across all required roles while limiting combination count for performance.
     */
    private List<ScoredStudent> rankCandidates(ProjectData project, List<StudentData> students) {
        Map<Long, ScoredStudent> uniqueById = new LinkedHashMap<>();
        for (StudentData student : students) {
            if (student == null) {
                continue;
            }
            uniqueById.putIfAbsent(student.id(), new ScoredStudent(student, calculateStudentScore(project, student)));
        }

        List<ScoredStudent> allScored = new ArrayList<>(uniqueById.values());
        allScored.sort((a, b) -> Double.compare(b.score(), a.score()));

        // Role-Diverse Selection: ensure top candidates for each required role are included
        java.util.Set<Long> selectedIds = new java.util.LinkedHashSet<>();
        List<ScoredStudent> diversePool = new ArrayList<>();

        if (project.requiredRoles() != null && !project.requiredRoles().isEmpty()) {
            for (String reqRole : project.requiredRoles()) {
                int countForRole = 0;
                for (ScoredStudent s : allScored) {
                    if (listContainsIgnoreCase(s.student().roles(), reqRole)) {
                        if (selectedIds.add(s.student().id())) {
                            diversePool.add(s);
                        }
                        countForRole++;
                        if (countForRole >= 3) break;
                    }
                }
            }
        }

        // Fill remaining candidate slots with top overall scoring students up to 15
        for (ScoredStudent s : allScored) {
            if (diversePool.size() >= 15) break;
            if (selectedIds.add(s.student().id())) {
                diversePool.add(s);
            }
        }

        return diversePool;
    }

    /**
     * Combines the four matcher scores using the weights from the
     * (Singleton) MatchingConfiguration.
     */
    private double calculateStudentScore(ProjectData project, StudentData student) {
        double skillScore = skillMatcher.score(project, student);
        double roleScore = roleMatcher.score(project, student);
        double interestScore = interestMatcher.score(project, student);
        double experienceScore = experienceMatcher.score(project, student);

        return (skillScore * config.skillWeight())
             + (roleScore * config.roleWeight())
             + (interestScore * config.interestWeight())
             + (experienceScore * config.experienceWeight());
    }

    /**
     * Recursively builds every possible team (combination, order does
     * not matter, no repeats) of size "teamSize" out of the candidate
     * list, and evaluates each finished team.
     */
    private void generateCombinations(
        List<ScoredStudent> candidates,
        int startIndex,
        int teamSize,
        List<ScoredStudent> currentTeam,
        ProjectData project,
        List<TeamRecommendation> output
    ) {
        if (currentTeam.size() == teamSize) {
            output.add(evaluateTeam(currentTeam, project));
            return;
        }

        for (int i = startIndex; i < candidates.size(); i++) {
            currentTeam.add(candidates.get(i));
            generateCombinations(candidates, i + 1, teamSize, currentTeam, project, output);
            currentTeam.remove(currentTeam.size() - 1);
        }
    }

    /**
     * Turns one candidate team into a full TeamRecommendation:
     * Evaluates complete team synergy by combining average individual score
     * with collective requirement coverage using exact weights (Skill 45%, Role 25%, Interest 20%, Experience 10%).
     */
    private TeamRecommendation evaluateTeam(List<ScoredStudent> teamMembers, ProjectData project) {

        // 1. Average individual score of members
        double totalIndividualScore = 0.0;
        for (ScoredStudent member : teamMembers) {
            totalIndividualScore += member.score();
        }
        double avgIndividualScore = teamMembers.isEmpty() ? 0.0 : totalIndividualScore / teamMembers.size();

        // 2. Collective team coverage & compatibility factors
        double skillCoverage = calculateSkillCoverage(teamMembers, project);
        double roleCoverage = calculateRoleCoverage(teamMembers, project);
        double interestCoverage = calculateInterestCoverage(teamMembers, project);
        double avgExperienceScore = calculateTeamExperienceScore(teamMembers, project);

        // 3. Team Synergy Score using Singleton configuration weights:
        // Skills = 45%, Roles = 25%, Interests = 20%, Experience = 10%
        double teamSynergyScore = (skillCoverage * config.skillWeight())
                                + (roleCoverage * config.roleWeight())
                                + (interestCoverage * config.interestWeight())
                                + (avgExperienceScore * config.experienceWeight());

        // 4. Final overall team recommendation score (50% individual talent + 50% team requirement synergy)
        double overallScore = (avgIndividualScore * 0.50) + (teamSynergyScore * 0.50);

        // ABSTRACT FACTORY: get typical role hints for this project type,
        // used as a last-resort fallback role for each member.
        RequirementBundle hints = ProjectTypeFactoryProvider.get(project.projectType()).createRequirements();

        List<MemberRecommendation> memberRecs = new ArrayList<>();
        for (int i = 0; i < teamMembers.size(); i++) {
            ScoredStudent member = teamMembers.get(i);

            String fallbackRole = (hints.roleHints() != null && i < hints.roleHints().size())
                ? hints.roleHints().get(i)
                : "Team Member";

            // BRIDGE: decide which role name to actually show for this member.
            String assignedRole = StudentRoleBridge.resolveAssignedRole(
                member.student().roles(),
                project.requiredRoles(),
                fallbackRole
            );

            double memberSkillScore = skillMatcher.score(project, member.student());
            double memberRoleScore = roleMatcher.score(project, member.student());
            double memberInterestScore = interestMatcher.score(project, member.student());
            double memberExperienceScore = experienceMatcher.score(project, member.student());

            memberRecs.add(new MemberRecommendation(
                member.student().id(),
                member.student().name(),
                assignedRole,
                round(member.score()),
                round(memberSkillScore),
                round(memberRoleScore),
                round(memberInterestScore),
                round(memberExperienceScore)
            ));
        }

        return new TeamRecommendation(
            round(overallScore),
            round(skillCoverage),
            round(roleCoverage),
            round(interestCoverage),
            memberRecs
        );
    }

    /** What % of the project's required skills (or skill slots) this team covers together. */
    private double calculateSkillCoverage(List<ScoredStudent> teamMembers, ProjectData project) {
        Map<String, Integer> requiredSkillCounts = project.requiredSkillCounts();

        if (requiredSkillCounts != null && !requiredSkillCounts.isEmpty()) {
            int requiredSlots = 0;
            int coveredSlots = 0;

            for (Map.Entry<String, Integer> entry : requiredSkillCounts.entrySet()) {
                String skillName = entry.getKey();
                int slotsNeeded = Math.max(1, entry.getValue());
                requiredSlots += slotsNeeded;

                int membersWithSkill = 0;
                for (ScoredStudent member : teamMembers) {
                    if (listContainsIgnoreCase(member.student().skills(), skillName)) {
                        membersWithSkill++;
                    }
                }
                coveredSlots += Math.min(slotsNeeded, membersWithSkill);
            }

            return requiredSlots == 0 ? 100.0 : (100.0 * coveredSlots) / requiredSlots;
        }

        List<String> requiredSkills = project.requiredSkills();
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 100.0;
        }

        int coveredSkills = 0;
        for (String requiredSkill : requiredSkills) {
            if (teamHasSkill(teamMembers, requiredSkill)) {
                coveredSkills++;
            }
        }
        return (100.0 * coveredSkills) / requiredSkills.size();
    }

    /** What % of the project's required roles (or role slots) this team covers together. */
    private double calculateRoleCoverage(List<ScoredStudent> teamMembers, ProjectData project) {
        Map<String, Integer> requiredRoleCounts = project.requiredRoleCounts();

        if (requiredRoleCounts != null && !requiredRoleCounts.isEmpty()) {
            int requiredSlots = 0;
            int coveredSlots = 0;

            for (Map.Entry<String, Integer> entry : requiredRoleCounts.entrySet()) {
                String roleName = entry.getKey();
                int slotsNeeded = Math.max(1, entry.getValue());
                requiredSlots += slotsNeeded;

                int membersWithRole = 0;
                for (ScoredStudent member : teamMembers) {
                    if (listContainsIgnoreCase(member.student().roles(), roleName)) {
                        membersWithRole++;
                    }
                }
                coveredSlots += Math.min(slotsNeeded, membersWithRole);
            }

            return requiredSlots == 0 ? 100.0 : (100.0 * coveredSlots) / requiredSlots;
        }

        List<String> requiredRoles = project.requiredRoles();
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return 100.0;
        }

        int coveredRoles = 0;
        for (String requiredRole : requiredRoles) {
            if (teamHasRole(teamMembers, requiredRole)) {
                coveredRoles++;
            }
        }
        return (100.0 * coveredRoles) / requiredRoles.size();
    }

    /** What % of the project's listed interests are covered by at least one team member. */
    private double calculateInterestCoverage(List<ScoredStudent> teamMembers, ProjectData project) {
        List<String> projectInterests = project.interests();

        if (projectInterests == null || projectInterests.isEmpty()) {
            return 100.0;
        }

        int coveredInterests = 0;
        for (String interest : projectInterests) {
            boolean covered = false;
            for (ScoredStudent member : teamMembers) {
                if (listContainsIgnoreCase(member.student().interests(), interest)) {
                    covered = true;
                    break;
                }
            }
            if (covered) {
                coveredInterests++;
            }
        }

        return (100.0 * coveredInterests) / projectInterests.size();
    }

    /** Calculates the average experience score across all candidate members of a team. */
    private double calculateTeamExperienceScore(List<ScoredStudent> teamMembers, ProjectData project) {
        if (teamMembers == null || teamMembers.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (ScoredStudent member : teamMembers) {
            sum += experienceMatcher.score(project, member.student());
        }
        return sum / teamMembers.size();
    }

    private boolean teamHasSkill(List<ScoredStudent> teamMembers, String skillName) {
        for (ScoredStudent member : teamMembers) {
            if (listContainsIgnoreCase(member.student().skills(), skillName)) {
                return true;
            }
        }
        return false;
    }

    private boolean teamHasRole(List<ScoredStudent> teamMembers, String roleName) {
        for (ScoredStudent member : teamMembers) {
            if (listContainsIgnoreCase(member.student().roles(), roleName)) {
                return true;
            }
        }
        return false;
    }

    private boolean listContainsIgnoreCase(List<String> list, String value) {
        if (list == null) {
            return false;
        }
        for (String item : list) {
            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    // Small private helper record pairing a student with their individual score.
    private record ScoredStudent(StudentData student, double score) {}
}
