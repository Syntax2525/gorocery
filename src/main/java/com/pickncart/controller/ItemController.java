package com.pickncart.controller;

import org.springframework.web.bind.annotation.*;
import com.pickncart.service.ItemService;
import com.pickncart.model.Item;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public Item create(@RequestBody Item item) {
        return itemService.save(item);
    }

    @GetMapping
    public List<Item> getAll() {
        return itemService.getAll();
    }

    @GetMapping("/{id}")
    public Item getById(@PathVariable Long id) {
        return itemService.getById(id).orElse(null);
    }

    @GetMapping("/search")
    public List<Item> search(@RequestParam String name) {
        return itemService.searchByName(name);
    }

    @PutMapping("/{id}")
    public Item update(@PathVariable Long id, @RequestBody Item item) {
        return itemService.update(id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.deleteById(id);
    }

    @GetMapping("/in-stock")
    public List<Item> getInStock() {
        return itemService.getInStockItems();
    }

    @GetMapping("/category/{categoryId}")
    public List<Item> getByCategory(@PathVariable Long categoryId) {
        return itemService.getItemsByCategory(categoryId);
    }
}
