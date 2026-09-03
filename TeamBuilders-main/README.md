# TeamBuilders

TeamBuilders is a campus team formation platform that helps college students create balanced project teams using **skills, preferred roles, interests, and experience**.

## Technology Stack

- Java 17
- Spring Boot
- SQLite
- JDBC
- DAO pattern
- HTML, CSS, JavaScript, Bootstrap
- Maven

The final application is completely Java-based. The database, CRUD operations, authentication, matching engine, teams, and invitations are handled by Java/Spring Boot through JDBC.

## Architecture

```text
Frontend (HTML/CSS/JS)
        |
        v
Spring Boot REST Controllers
        |
        v
Service / Business Logic
        |
        v
DAO Interfaces + JDBC Implementations
        |
        v
DatabaseManager (Singleton)
        |
        v
SQLite
```

## Database

The application creates and manages its SQLite database automatically through JDBC.

Main tables:

- students
- skills
- student_skills
- interests
- student_interests
- roles
- student_roles
- projects
- project_skills
- project_roles
- project_interests
- teams
- team_members
- invitations

## DAO Layer

Each major entity has a DAO interface and JDBC implementation:

- StudentDAO / StudentDAOImpl
- SkillDAO / SkillDAOImpl
- InterestDAO / InterestDAOImpl
- RoleDAO / RoleDAOImpl
- ProjectDAO / ProjectDAOImpl
- TeamDAO / TeamDAOImpl
- InvitationDAO / InvitationDAOImpl

Controllers never execute SQL directly. DAOs use `PreparedStatement`, `ResultSet`, JDBC transactions, and the Singleton `DatabaseManager`.

## Matching Engine

The matching engine ranks candidates and recommends team combinations using:

| Factor | Weight |
|---|---:|
| Skill Match | 45% |
| Role Match | 25% |
| Interest Match | 20% |
| Experience Match | 10% |

Availability is intentionally not part of the matching formula. Team recommendations also consider collective skill and role coverage so a balanced team can outperform a group of students with similar profiles.

## Design Patterns

### Singleton
`DatabaseManager` provides one centralized JDBC database manager instance.

### Factory
`MatcherFactory` creates the appropriate matching component.

### Abstract Factory
Project-type factories create project-specific requirement bundles.

### Bridge
`StudentRoleBridge` separates student role information from project role handling.

### Proxy + Real Subject
`MatchingProxy` controls access to `RealMatchingService`, which performs the actual matching and team formation.

Strategy pattern is intentionally not used.

## Running the Project

Requirements:

- Java 17+
- Maven

Run:

```bash
cd java_matching_engine
mvn spring-boot:run
```

Then open:

```text
http://localhost:8081/
```

The first application start creates the SQLite database and demo data automatically.

Demo accounts use the password `password123`.

## Main API Groups

```text
/api/auth
/api/students
/api/skills
/api/interests
/api/roles
/api/projects
/api/teams
/api/invitations
/api/matching
```

## Project Workflow

1. Student registers/logs in.
2. Student manages profile, skills, interests, and preferred roles.
3. Student creates a project.
4. Project requirements are added.
5. Java matching engine evaluates candidates.
6. Recommended teams are displayed.
7. Project owner forms a team or sends invitations.
8. Students accept/reject invitations.
9. Team membership is managed and finalized.
