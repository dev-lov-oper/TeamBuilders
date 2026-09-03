package com.campus.matching.dao;

import com.campus.matching.model.Skill;

import java.util.List;
import java.util.Optional;

public interface SkillDAO {
    Skill createSkill(Skill skill);
    Optional<Skill> getSkillById(Long id);
    Optional<Skill> getSkillByName(String name);
    List<Skill> getAllSkills();
    Skill updateSkill(Skill skill);
    boolean deleteSkill(Long id);
}
