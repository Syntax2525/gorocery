package com.pickncart.service;

import org.springframework.stereotype.Service;
import com.pickncart.repository.SuggestionRepository;
import com.pickncart.model.Suggestion;
import com.pickncart.model.User;
import java.util.List;

@Service
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;

    public SuggestionService(SuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    public Suggestion save(Suggestion suggestion) {
        return suggestionRepository.save(suggestion);
    }

    public List<Suggestion> getUserSuggestions(User user) {
        return suggestionRepository.findByUser(user);
    }

    public List<Suggestion> getAll() {
        return suggestionRepository.findAll();
    }
}