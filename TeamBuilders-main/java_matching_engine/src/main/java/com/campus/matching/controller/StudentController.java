package com.campus.matching.controller;

import com.campus.matching.dao.StudentDAO;
import com.campus.matching.model.Student;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller exposing HTTP CRUD endpoints for Student management.
 * Delegates all database interactions to StudentDAO (DAO pattern).
 */
@RestController
@RequestMapping("/api/students")
@CrossOrigin
public class StudentController {

    private final StudentDAO studentDAO;

    public StudentController(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    /**
     * POST /api/students - Create a new student.
     */
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        if (student == null || student.getUsername() == null || student.getUsername().isBlank()) {
            throw new com.campus.matching.exception.BadRequestException("Student username is required.");
        }
        if (student.getEmail() == null || student.getEmail().isBlank()) {
            throw new com.campus.matching.exception.BadRequestException("Student email is required.");
        }
        Student created = studentDAO.createStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/students - Retrieve all students.
     */
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentDAO.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * GET /api/students/{id} - Retrieve student by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new com.campus.matching.exception.BadRequestException("Invalid student ID: " + id);
        }
        Optional<Student> studentOpt = studentDAO.getStudentById(id);
        return studentOpt.map(ResponseEntity::ok)
                .orElseThrow(() -> new com.campus.matching.exception.ResourceNotFoundException("Student with ID " + id + " does not exist."));
    }

    /**
     * PUT /api/students/{id} - Update student by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        if (id == null || id <= 0) {
            throw new com.campus.matching.exception.BadRequestException("Invalid student ID: " + id);
        }
        if (student == null) {
            throw new com.campus.matching.exception.BadRequestException("Student payload is required.");
        }

        if (studentDAO.getStudentById(id).isEmpty()) {
            throw new com.campus.matching.exception.ResourceNotFoundException("Student with ID " + id + " does not exist.");
        }

        student.setId(id);
        Student updated = studentDAO.updateStudent(student);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/students/{id} - Delete student by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new com.campus.matching.exception.BadRequestException("Invalid student ID: " + id);
        }
        boolean deleted = studentDAO.deleteStudent(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        throw new com.campus.matching.exception.ResourceNotFoundException("Student with ID " + id + " does not exist.");
    }

    // ============================================================
    // RELATIONSHIP ENDPOINTS (student_skills, student_interests, student_roles)
    // ============================================================

    @PostMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Void> addSkillToStudent(@PathVariable Long id, @PathVariable Long skillId) {
        boolean added = studentDAO.addSkillToStudent(id, skillId);
        return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Void> removeSkillFromStudent(@PathVariable Long id, @PathVariable Long skillId) {
        boolean removed = studentDAO.removeSkillFromStudent(id, skillId);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<com.campus.matching.model.Skill>> getStudentSkills(@PathVariable Long id) {
        return ResponseEntity.ok(studentDAO.getStudentSkills(id));
    }

    @PostMapping("/{id}/interests/{interestId}")
    public ResponseEntity<Void> addInterestToStudent(@PathVariable Long id, @PathVariable Long interestId) {
        boolean added = studentDAO.addInterestToStudent(id, interestId);
        return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}/interests/{interestId}")
    public ResponseEntity<Void> removeInterestFromStudent(@PathVariable Long id, @PathVariable Long interestId) {
        boolean removed = studentDAO.removeInterestFromStudent(id, interestId);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/interests")
    public ResponseEntity<List<com.campus.matching.model.Interest>> getStudentInterests(@PathVariable Long id) {
        return ResponseEntity.ok(studentDAO.getStudentInterests(id));
    }

    @PostMapping("/{id}/roles/{roleId}")
    public ResponseEntity<Void> addRoleToStudent(@PathVariable Long id, @PathVariable Long roleId) {
        boolean added = studentDAO.addRoleToStudent(id, roleId);
        return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromStudent(@PathVariable Long id, @PathVariable Long roleId) {
        boolean removed = studentDAO.removeRoleFromStudent(id, roleId);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<List<com.campus.matching.model.Role>> getStudentRoles(@PathVariable Long id) {
        return ResponseEntity.ok(studentDAO.getStudentRoles(id));
    }
}
