package com.campus.matching.controller;

import com.campus.matching.auth.PasswordUtil;
import com.campus.matching.dao.InterestDAO;
import com.campus.matching.dao.RoleDAO;
import com.campus.matching.dao.SkillDAO;
import com.campus.matching.dao.StudentDAO;
import com.campus.matching.model.Interest;
import com.campus.matching.model.Role;
import com.campus.matching.model.Skill;
import com.campus.matching.model.Student;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private static final String SESSION_USER_ID = "teamBuildersUserId";

    private final StudentDAO studentDAO;
    private final SkillDAO skillDAO;
    private final InterestDAO interestDAO;
    private final RoleDAO roleDAO;

    public AuthController(StudentDAO studentDAO, SkillDAO skillDAO, InterestDAO interestDAO, RoleDAO roleDAO) {
        this.studentDAO = studentDAO;
        this.skillDAO = skillDAO;
        this.interestDAO = interestDAO;
        this.roleDAO = roleDAO;
    }

    @PostMapping("/register")
    public ResponseEntity<Student> register(@RequestBody Map<String, Object> payload, HttpSession session) {
        String username = text(payload.get("username"));
        String email = text(payload.get("email"));
        String password = text(payload.get("password"));
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (studentDAO.getAllStudents().stream().anyMatch(s -> s.getUsername().equalsIgnoreCase(username) || s.getEmail().equalsIgnoreCase(email))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Student student = new Student();
        student.setUsername(username);
        student.setEmail(email);
        student.setDepartment(text(payload.get("department")));
        student.setYear(number(payload.get("year"), 3));
        student.setExperienceLevel(text(payload.getOrDefault("experience_level", "INTERMEDIATE")));
        student.setBio(text(payload.get("bio")));
        student.setPasswordHash(PasswordUtil.hash(password));

        Student created = studentDAO.createStudent(student);
        session.setAttribute(SESSION_USER_ID, created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> payload, HttpSession session) {
        String username = text(payload.get("username"));
        String password = text(payload.get("password"));
        return studentDAO.authenticate(username, PasswordUtil.hash(password))
                .map(student -> {
                    session.setAttribute(SESSION_USER_ID, student.getId());
                    return ResponseEntity.ok((Object) student);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid username or password.")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Student> me(HttpSession session) {
        Long id = userId(session);
        if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return studentDAO.getStudentById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PutMapping("/profile")
    public ResponseEntity<Student> updateProfile(@RequestBody Student payload, HttpSession session) {
        Long id = userId(session);
        if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Student existing = studentDAO.getStudentById(id).orElse(null);
        if (existing == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        existing.setDepartment(payload.getDepartment());
        existing.setYear(payload.getYear());
        existing.setExperienceLevel(payload.getExperienceLevel());
        existing.setBio(payload.getBio());
        return ResponseEntity.ok(studentDAO.updateProfile(existing));
    }

    @PostMapping("/profile/skills")
    public ResponseEntity<Void> addSkill(@RequestBody Map<String, Object> payload, HttpSession session) {
        Long id = userId(session); if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String name = text(payload.get("name")); if (name.isBlank()) return ResponseEntity.badRequest().build();
        Skill skill = skillDAO.getSkillByName(name).orElseGet(() -> skillDAO.createSkill(new Skill(null, name)));
        return studentDAO.addSkillToStudent(id, skill.getId()) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/profile/skills/{skillId}")
    public ResponseEntity<Void> removeSkill(@PathVariable Long skillId, HttpSession session) {
        Long id = userId(session); if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return studentDAO.removeSkillFromStudent(id, skillId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/profile/interests")
    public ResponseEntity<Void> addInterest(@RequestBody Map<String, Object> payload, HttpSession session) {
        Long id = userId(session); if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String name = text(payload.get("name")); if (name.isBlank()) return ResponseEntity.badRequest().build();
        Interest item = interestDAO.getInterestByName(name).orElseGet(() -> interestDAO.createInterest(new Interest(null, name)));
        return studentDAO.addInterestToStudent(id, item.getId()) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/profile/interests/{interestId}")
    public ResponseEntity<Void> removeInterest(@PathVariable Long interestId, HttpSession session) {
        Long id = userId(session); if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return studentDAO.removeInterestFromStudent(id, interestId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/profile/roles")
    public ResponseEntity<Void> addRole(@RequestBody Map<String, Object> payload, HttpSession session) {
        Long id = userId(session); if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String name = text(payload.get("name")); if (name.isBlank()) return ResponseEntity.badRequest().build();
        Role role = roleDAO.getRoleByName(name).orElseGet(() -> roleDAO.createRole(new Role(null, name)));
        return studentDAO.addRoleToStudent(id, role.getId()) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/profile/roles/{roleId}")
    public ResponseEntity<Void> removeRole(@PathVariable Long roleId, HttpSession session) {
        Long id = userId(session); if (id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return studentDAO.removeRoleFromStudent(id, roleId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/users")
    public ResponseEntity<java.util.List<Student>> users(HttpSession session) {
        if (userId(session) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(studentDAO.getAllStudents());
    }

    private Long userId(HttpSession session) {
        Object value = session.getAttribute(SESSION_USER_ID);
        return value instanceof Number n ? n.longValue() : null;
    }

    private String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
    private int number(Object value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException e) { return fallback; }
    }
}
