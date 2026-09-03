package com.campus.matching;

import com.campus.matching.dao.StudentDAO;
import com.campus.matching.dao.StudentDAOImpl;
import com.campus.matching.db.DatabaseInitializer;
import com.campus.matching.model.Interest;
import com.campus.matching.model.Role;
import com.campus.matching.model.Skill;
import com.campus.matching.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StudentDAOTest {

    private StudentDAO studentDAO;

    @BeforeEach
    public void setUp() {
        // Initialize schema
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        studentDAO = new StudentDAOImpl();
    }

    @Test
    public void testCreateAndGetStudent() {
        String uniqueUser = "test_user_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = uniqueUser + "@campus.edu";

        Student student = new Student(null, uniqueUser, uniqueEmail, "Computer Science", 3, "ADVANCED", "Passionate developer");
        student.setSkills(List.of(new Skill("Java"), new Skill("Spring Boot")));
        student.setInterests(List.of(new Interest("AI"), new Interest("Web Dev")));
        student.setPreferredRoles(List.of(new Role("Backend Developer")));

        Student created = studentDAO.createStudent(student);
        assertNotNull(created.getId(), "Created student should have a generated primary key ID");
        assertEquals(uniqueUser, created.getUsername());

        Optional<Student> fetchedOpt = studentDAO.getStudentById(created.getId());
        assertTrue(fetchedOpt.isPresent(), "Fetched student should be present");
        Student fetched = fetchedOpt.get();

        assertEquals("Computer Science", fetched.getDepartment());
        assertEquals("ADVANCED", fetched.getExperienceLevel());
        assertFalse(fetched.getSkills().isEmpty(), "Associated skills should be retrieved");
        assertFalse(fetched.getInterests().isEmpty(), "Associated interests should be retrieved");
        assertFalse(fetched.getPreferredRoles().isEmpty(), "Associated preferred roles should be retrieved");
    }

    @Test
    public void testGetAllStudents() {
        String uniqueUser = "list_user_" + UUID.randomUUID().toString().substring(0, 8);
        Student student = new Student(null, uniqueUser, uniqueUser + "@campus.edu", "Electrical Engineering", 2, "BEGINNER", "Embedded systems enthusiast");
        studentDAO.createStudent(student);

        List<Student> students = studentDAO.getAllStudents();
        assertNotNull(students);
        assertFalse(students.isEmpty(), "getAllStudents should return at least one student");
    }

    @Test
    public void testUpdateStudent() {
        String uniqueUser = "update_user_" + UUID.randomUUID().toString().substring(0, 8);
        Student student = new Student(null, uniqueUser, uniqueUser + "@campus.edu", "Physics", 1, "BEGINNER", "Original bio");
        Student created = studentDAO.createStudent(student);

        created.setDepartment("Data Science");
        created.setExperienceLevel("ADVANCED");
        created.setBio("Updated bio for test");
        created.setSkills(List.of(new Skill("Java"), new Skill("TensorFlow")));

        Student updated = studentDAO.updateStudent(created);
        assertEquals("Data Science", updated.getDepartment());
        assertEquals("ADVANCED", updated.getExperienceLevel());
        assertEquals("Updated bio for test", updated.getBio());

        Optional<Student> refetchedOpt = studentDAO.getStudentById(created.getId());
        assertTrue(refetchedOpt.isPresent());
        assertEquals("Data Science", refetchedOpt.get().getDepartment());
        assertTrue(refetchedOpt.get().getSkills().stream().anyMatch(s -> s.getName().equalsIgnoreCase("Java")));
    }

    @Test
    public void testDeleteStudent() {
        String uniqueUser = "delete_user_" + UUID.randomUUID().toString().substring(0, 8);
        Student student = new Student(null, uniqueUser, uniqueUser + "@campus.edu", "Math", 4, "INTERMEDIATE", "To be deleted");
        Student created = studentDAO.createStudent(student);

        Long studentId = created.getId();
        assertTrue(studentDAO.getStudentById(studentId).isPresent(), "Student should exist before deletion");

        boolean deleted = studentDAO.deleteStudent(studentId);
        assertTrue(deleted, "deleteStudent should return true");
        assertTrue(studentDAO.getStudentById(studentId).isEmpty(), "Student should no longer exist after deletion");
    }
}
