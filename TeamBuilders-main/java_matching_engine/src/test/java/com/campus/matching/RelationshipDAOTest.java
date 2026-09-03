package com.campus.matching;

import com.campus.matching.dao.*;
import com.campus.matching.db.DatabaseInitializer;
import com.campus.matching.model.Interest;
import com.campus.matching.model.Role;
import com.campus.matching.model.Skill;
import com.campus.matching.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipDAOTest {

    private SkillDAO skillDAO;
    private InterestDAO interestDAO;
    private RoleDAO roleDAO;
    private StudentDAO studentDAO;

    @BeforeEach
    public void setUp() {
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeSchema();

        skillDAO = new SkillDAOImpl();
        interestDAO = new InterestDAOImpl();
        roleDAO = new RoleDAOImpl();
        studentDAO = new StudentDAOImpl();
    }

    @Test
    public void testSkillCRUD() {
        String skillName = "Skill_" + UUID.randomUUID().toString().substring(0, 6);
        Skill skill = skillDAO.createSkill(new Skill(skillName));
        assertNotNull(skill.getId());

        assertTrue(skillDAO.getSkillById(skill.getId()).isPresent());
        assertTrue(skillDAO.getSkillByName(skillName).isPresent());

        skill.setName(skillName + "_updated");
        Skill updated = skillDAO.updateSkill(skill);
        assertEquals(skillName + "_updated", updated.getName());

        assertTrue(skillDAO.deleteSkill(skill.getId()));
        assertTrue(skillDAO.getSkillById(skill.getId()).isEmpty());
    }

    @Test
    public void testInterestCRUD() {
        String interestName = "Interest_" + UUID.randomUUID().toString().substring(0, 6);
        Interest interest = interestDAO.createInterest(new Interest(interestName));
        assertNotNull(interest.getId());

        assertTrue(interestDAO.getInterestById(interest.getId()).isPresent());
        assertTrue(interestDAO.getInterestByName(interestName).isPresent());

        interest.setName(interestName + "_updated");
        Interest updated = interestDAO.updateInterest(interest);
        assertEquals(interestName + "_updated", updated.getName());

        assertTrue(interestDAO.deleteInterest(interest.getId()));
        assertTrue(interestDAO.getInterestById(interest.getId()).isEmpty());
    }

    @Test
    public void testRoleCRUD() {
        String roleName = "Role_" + UUID.randomUUID().toString().substring(0, 6);
        Role role = roleDAO.createRole(new Role(roleName));
        assertNotNull(role.getId());

        assertTrue(roleDAO.getRoleById(role.getId()).isPresent());
        assertTrue(roleDAO.getRoleByName(roleName).isPresent());

        role.setName(roleName + "_updated");
        Role updated = roleDAO.updateRole(role);
        assertEquals(roleName + "_updated", updated.getName());

        assertTrue(roleDAO.deleteRole(role.getId()));
        assertTrue(roleDAO.getRoleById(role.getId()).isEmpty());
    }

    @Test
    public void testStudentSkillRelationship() {
        String user = "rel_user_" + UUID.randomUUID().toString().substring(0, 6);
        Student student = studentDAO.createStudent(new Student(null, user, user + "@campus.edu", "CS", 3, "INTERMEDIATE", "Bio"));
        Skill skill = skillDAO.createSkill(new Skill("GoLang_" + UUID.randomUUID().toString().substring(0, 4)));

        assertTrue(studentDAO.addSkillToStudent(student.getId(), skill.getId()));

        List<Skill> skills = studentDAO.getStudentSkills(student.getId());
        assertFalse(skills.isEmpty());
        assertTrue(skills.stream().anyMatch(s -> s.getId().equals(skill.getId())));

        assertTrue(studentDAO.removeSkillFromStudent(student.getId(), skill.getId()));
        List<Skill> skillsAfterRemove = studentDAO.getStudentSkills(student.getId());
        assertTrue(skillsAfterRemove.stream().noneMatch(s -> s.getId().equals(skill.getId())));
    }

    @Test
    public void testStudentInterestRelationship() {
        String user = "rel_user_int_" + UUID.randomUUID().toString().substring(0, 6);
        Student student = studentDAO.createStudent(new Student(null, user, user + "@campus.edu", "CS", 3, "INTERMEDIATE", "Bio"));
        Interest interest = interestDAO.createInterest(new Interest("Robotics_" + UUID.randomUUID().toString().substring(0, 4)));

        assertTrue(studentDAO.addInterestToStudent(student.getId(), interest.getId()));

        List<Interest> interests = studentDAO.getStudentInterests(student.getId());
        assertFalse(interests.isEmpty());
        assertTrue(interests.stream().anyMatch(i -> i.getId().equals(interest.getId())));

        assertTrue(studentDAO.removeInterestFromStudent(student.getId(), interest.getId()));
        List<Interest> interestsAfterRemove = studentDAO.getStudentInterests(student.getId());
        assertTrue(interestsAfterRemove.stream().noneMatch(i -> i.getId().equals(interest.getId())));
    }

    @Test
    public void testStudentRoleRelationship() {
        String user = "rel_user_role_" + UUID.randomUUID().toString().substring(0, 6);
        Student student = studentDAO.createStudent(new Student(null, user, user + "@campus.edu", "CS", 3, "INTERMEDIATE", "Bio"));
        Role role = roleDAO.createRole(new Role("DevOps Architect_" + UUID.randomUUID().toString().substring(0, 4)));

        assertTrue(studentDAO.addRoleToStudent(student.getId(), role.getId()));

        List<Role> roles = studentDAO.getStudentRoles(student.getId());
        assertFalse(roles.isEmpty());
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals(role.getId())));

        assertTrue(studentDAO.removeRoleFromStudent(student.getId(), role.getId()));
        List<Role> rolesAfterRemove = studentDAO.getStudentRoles(student.getId());
        assertTrue(rolesAfterRemove.stream().noneMatch(r -> r.getId().equals(role.getId())));
    }
}
