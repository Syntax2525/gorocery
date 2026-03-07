package com.pickncart.controller;

import org.springframework.web.bind.annotation.*;
import com.pickncart.service.SuggestionService;
import com.pickncart.model.Suggestion;
import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping
    public Suggestion create(@RequestBody Suggestion suggestion) {
        return suggestionService.save(suggestion);
    }

    @GetMapping
    public List<Suggestion> getAll() {
        return suggestionService.getAll();
    }
}