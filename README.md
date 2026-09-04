# TeamBuilders — Design Patterns Implementation

## Overview

**TeamBuilders** is a Java-based campus team formation system that recommends project teams by matching students according to their **skills, roles, interests, and experience**.

The project demonstrates the practical use of multiple **Design Patterns** within a real team-matching workflow.

---

## Design Patterns Used

### 1. Proxy Pattern

**Package:** `service`

**Classes:**

* `MatchingService`
* `MatchingProxy`
* `RealMatchingService`

The Proxy controls access to the actual matching engine.

```text
MatchingController
        ↓
MatchingProxy
        ↓
RealMatchingService
```

`MatchingProxy` performs basic validation before forwarding valid requests to the expensive matching algorithm.

**Purpose:** Control access to the real service and prevent unnecessary execution of the matching algorithm.

---

### 2. Factory Method Pattern

**Package:** `factory`

**Classes:**

* `Matcher`
* `MatcherFactory`
* `SkillMatcher`
* `RoleMatcher`
* `InterestMatcher`
* `ExperienceMatcher`

The matching system uses different strategies for evaluating students.

```text
                 Matcher
                    |
        +-----------+-----------+
        |           |           |
      Skill        Role      Interest
     Matcher      Matcher      Matcher
                    +
              ExperienceMatcher
```

`MatcherFactory` creates the appropriate matcher based on the requested type.

```text
"SKILL"      → SkillMatcher
"ROLE"       → RoleMatcher
"INTEREST"   → InterestMatcher
"EXPERIENCE" → ExperienceMatcher
```

**Purpose:** Centralize matcher creation and allow new matcher types to be added without changing the main matching logic.

---

### 3. Abstract Factory Pattern

**Package:** `abstractfactory`

**Classes:**

* `ProjectTypeFactory`
* `ProjectTypeFactoryProvider`
* `WebProjectFactory`
* `MlProjectFactory`
* `HardwareProjectFactory`
* `OtherProjectFactory`
* `RequirementBundle`

Different project types require different sets of skills, roles, and interests.

```text
              ProjectTypeFactory
                     |
       +-------------+-------------+
       |             |             |
      WEB            ML         HARDWARE
       |             |             |
 WebProject      MlProject     HardwareProject
  Factory          Factory        Factory
       |             |             |
       +-------------+-------------+
                     ↓
            RequirementBundle
```

For example, an ML project can provide:

```text
Skills:
Machine Learning
TensorFlow
PyTorch

Roles:
ML Engineer
Data Scientist
Data Engineer
```

`ProjectTypeFactoryProvider` selects the appropriate factory for the project type.

**Purpose:** Create a complete set of project-specific requirements while keeping project-type logic separate.

---

### 4. Singleton Pattern

**Classes:**

* `MatchingConfiguration`
* `DatabaseManager`

`MatchingConfiguration` maintains the common matching weights used throughout the application.

```text
MatchingConfiguration
        ↓
Skill       → 45%
Role        → 25%
Interest    → 20%
Experience  → 10%
```

The configuration is accessed through:

```java
MatchingConfiguration.getInstance();
```

`DatabaseManager` similarly provides a centralized database-management instance.

**Purpose:** Ensure shared resources and configuration have a single consistent instance across the application.

---

### 5. Bridge Pattern

**Package:** `bridge`

**Classes:**

* `ProjectRole`
* `ConcreteProjectRole`
* `StudentRoleBridge`

The Bridge separates **role assignment logic** from the representation of a project role.

```text
Student Roles
      +
Project Roles
      ↓
StudentRoleBridge
      ↓
Assigned Project Role
```

The bridge determines the most appropriate role for a student based on project requirements and the student's available roles.

**Purpose:** Keep role-selection logic independent from the way roles are represented.

---

## DAO Pattern

Although DAO is an architectural pattern rather than a GoF behavioral/structural/creational pattern, it is an important part of the project.

**Packages:**

```text
dao
```

Examples:

```text
StudentDAO
StudentDAOImpl

ProjectDAO
ProjectDAOImpl

TeamDAO
TeamDAOImpl
```

The DAO layer separates database operations from business logic.

```text
Service
   ↓
DAO Interface
   ↓
DAO Implementation
   ↓
JDBC
   ↓
SQLite
```

**Purpose:** Keep SQL/JDBC code isolated from the matching and application logic.

---

# Overall Design Pattern Flow

The patterns work together during team recommendation:

```text
User Request
     ↓
MatchingController
     ↓
MatchingProxy
     ↓
RealMatchingService
     |
     +── MatcherFactory
     |       ↓
     |   Skill / Role /
     |   Interest / Experience
     |   Matchers
     |
     +── MatchingConfiguration
     |       ↓
     |   Common Weights
     |
     +── ProjectTypeFactoryProvider
     |       ↓
     |   Project-specific
     |   RequirementBundle
     |
     +── StudentRoleBridge
     |       ↓
     |   Assigned Roles
     |
     +── DAO Layer
             ↓
         JDBC / SQLite
```

The result is a modular matching system where each design pattern has a specific responsibility:

| Pattern              | Main Responsibility                                |
| -------------------- | -------------------------------------------------- |
| **Proxy**            | Controls access to the matching service            |
| **Factory Method**   | Creates the appropriate matcher                    |
| **Abstract Factory** | Creates project-specific requirement bundles       |
| **Singleton**        | Provides shared configuration/database management  |
| **Bridge**           | Separates role assignment from role representation |
| **DAO**              | Separates database access from business logic      |

---

## Technology

* Java 17
* Spring Boot
* JDBC
* SQLite
* Maven
* HTML/CSS/JavaScript
