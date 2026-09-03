package com.campus.matching;

import com.campus.matching.dao.*;
import com.campus.matching.db.DatabaseDemoDataInitializer;
import com.campus.matching.db.DatabaseInitializer;
import com.campus.matching.model.Project;
import com.campus.matching.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DemoDataInitializerTest {

    private StudentDAO studentDAO;
    private ProjectDAO projectDAO;
    private DatabaseDemoDataInitializer demoInitializer;

    @BeforeEach
    public void setUp() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        demoInitializer = new DatabaseDemoDataInitializer();
        studentDAO = new StudentDAOImpl();
        projectDAO = new ProjectDAOImpl();
    }

    @Test
    public void testDemoDataSeedingAndIdempotency() {
        // Run seeding first time
        demoInitializer.seedDemoData();

        List<Student> studentsFirstRun = studentDAO.getAllStudents();
        assertTrue(studentsFirstRun.size() >= 10, "Should have at least 10 demo students");

        List<Project> projectsFirstRun = projectDAO.getAllProjects();
        assertTrue(projectsFirstRun.size() >= 4, "Should have at least 4 demo projects");

        int studentCountFirst = studentsFirstRun.size();
        int projectCountFirst = projectsFirstRun.size();

        // Run seeding second time (idempotency check)
        demoInitializer.seedDemoData();

        List<Student> studentsSecondRun = studentDAO.getAllStudents();
        List<Project> projectsSecondRun = projectDAO.getAllProjects();

        assertEquals(studentCountFirst, studentsSecondRun.size(), "Idempotent seeding should not create duplicate students");
        assertEquals(projectCountFirst, projectsSecondRun.size(), "Idempotent seeding should not create duplicate projects");
    }

    @Test
    public void testStudentProfileVariations() {
        demoInitializer.seedDemoData();

        List<Student> allStudents = studentDAO.getAllStudents();
        Student alex = allStudents.stream().filter(s -> "alex_skills_master".equals(s.getUsername())).findFirst().orElse(null);
        Student morgan = allStudents.stream().filter(s -> "dr_morgan_expert".equals(s.getUsername())).findFirst().orElse(null);

        assertNotNull(alex, "alex_skills_master should exist");
        assertNotNull(morgan, "dr_morgan_expert should exist");

        assertEquals("ADVANCED", morgan.getExperienceLevel());
        assertFalse(studentDAO.getStudentSkills(alex.getId()).isEmpty(), "Alex should have skills populated");
    }
}
