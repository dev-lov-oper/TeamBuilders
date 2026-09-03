package com.campus.matching.dao;

import com.campus.matching.model.Interest;

import java.util.List;
import java.util.Optional;

public interface InterestDAO {
    Interest createInterest(Interest interest);
    Optional<Interest> getInterestById(Long id);
    Optional<Interest> getInterestByName(String name);
    List<Interest> getAllInterests();
    Interest updateInterest(Interest interest);
    boolean deleteInterest(Long id);
}
