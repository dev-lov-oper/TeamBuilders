package com.campus.matching.controller;

import com.campus.matching.dao.SkillDAO;
import com.campus.matching.model.Skill;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin
public class SkillController {

    private final SkillDAO skillDAO;

    public SkillController(SkillDAO skillDAO) {
        this.skillDAO = skillDAO;
    }

    @PostMapping
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) {
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(skillDAO.createSkill(skill));
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillDAO.getAllSkills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        return skillDAO.getSkillById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        if (skill == null || skill.getName() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (skillDAO.getSkillById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        skill.setId(id);
        return ResponseEntity.ok(skillDAO.updateSkill(skill));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        if (skillDAO.deleteSkill(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
