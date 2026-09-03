package com.campus.matching.dao;

import com.campus.matching.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * ================================
 *  DESIGN PATTERN: DATA ACCESS OBJECT (DAO)
 * ================================
 * Interface defining abstract CRUD operations for Student data persistence.
 * Decouples business logic / API controllers from underlying database storage details.
 */
public interface StudentDAO {

    /**
     * Inserts a new student record and populates associated skills, interests, and preferred roles.
     *
     * @param student Student model containing details and skill/interest/role collections
     * @return Saved Student object with generated primary key ID populated
     */
    Student createStudent(Student student);

    /**
     * Retrieves a student by primary key ID along with their skills, interests, and roles.
     *
     * @param id Primary key ID of student
     * @return Optional containing Student if found, or empty Optional
     */
    Optional<Student> getStudentById(Long id);

    /**
     * Retrieves all student records along with their associated skills, interests, and roles.
     *
     * @return List of all students
     */
    List<Student> getAllStudents();

    /**
     * Updates an existing student record and synchronizes associated skills, interests, and roles.
     *
     * @param student Student model containing updated values
     * @return Updated Student object
     */
    Student updateStudent(Student student);

    /**
     * Deletes a student by primary key ID.
     *
     * @param id Primary key ID of student to delete
     * @return true if record was deleted, false otherwise
     */
    boolean deleteStudent(Long id);

    Optional<Student> authenticate(String username, String passwordHash);
    Student updateProfile(Student student);

    // ============================================================
    // RELATIONSHIP METHODS (student_skills, student_interests, student_roles)
    // ============================================================

    boolean addSkillToStudent(Long studentId, Long skillId);
    boolean removeSkillFromStudent(Long studentId, Long skillId);
    List<com.campus.matching.model.Skill> getStudentSkills(Long studentId);

    boolean addInterestToStudent(Long studentId, Long interestId);
    boolean removeInterestFromStudent(Long studentId, Long interestId);
    List<com.campus.matching.model.Interest> getStudentInterests(Long studentId);

    boolean addRoleToStudent(Long studentId, Long roleId);
    boolean removeRoleFromStudent(Long studentId, Long roleId);
    List<com.campus.matching.model.Role> getStudentRoles(Long studentId);
}
