package com.campus.matching.controller;

import com.campus.matching.dao.InterestDAO;
import com.campus.matching.model.Interest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@CrossOrigin
public class InterestController {

    private final InterestDAO interestDAO;

    public InterestController(InterestDAO interestDAO) {
        this.interestDAO = interestDAO;
    }

    @PostMapping
    public ResponseEntity<Interest> createInterest(@RequestBody Interest interest) {
        if (interest == null || interest.getName() == null || interest.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(interestDAO.createInterest(interest));
    }

    @GetMapping
    public ResponseEntity<List<Interest>> getAllInterests() {
        return ResponseEntity.ok(interestDAO.getAllInterests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interest> getInterestById(@PathVariable Long id) {
        return interestDAO.getInterestById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Interest> updateInterest(@PathVariable Long id, @RequestBody Interest interest) {
        if (interest == null || interest.getName() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (interestDAO.getInterestById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        interest.setId(id);
        return ResponseEntity.ok(interestDAO.updateInterest(interest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterest(@PathVariable Long id) {
        if (interestDAO.deleteInterest(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
