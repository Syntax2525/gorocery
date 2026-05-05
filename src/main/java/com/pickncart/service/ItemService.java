package com.pickncart.service;

import com.pickncart.model.Category;
import com.pickncart.model.Item;
import com.pickncart.repository.CategoryRepository;
import com.pickncart.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public List<Item> getAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> getById(Long id) {
        return itemRepository.findById(id);
    }

    public List<Item> searchByName(String name) {
        return itemRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public List<Item> getItemsByCategory(Long id) {
        return itemRepository.findByCategoryId(id);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    public Item update(Long id, Item updatedItem) {
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        existingItem.setName(updatedItem.getName());
        existingItem.setDescription(updatedItem.getDescription());
        existingItem.setPrice(updatedItem.getPrice());
        existingItem.setStock(updatedItem.getStock());
        existingItem.setImageUrl(updatedItem.getImageUrl());

        if (updatedItem.getCategory() != null) {
            existingItem.setCategory(updatedItem.getCategory());
        }

        return itemRepository.save(existingItem);
    }

    public List<Item> getInStockItems() {
        return itemRepository.findByStockGreaterThan(0);
    }

    public List<Item> getInStockItemsByCategory(Long categoryId) {
        return itemRepository.findByCategoryIdAndStockGreaterThan(categoryId, 0);
    }
}
