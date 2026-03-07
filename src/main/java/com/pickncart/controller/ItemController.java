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

    @GetMapping("/search")
    public List<Item> search(@RequestParam String name) {
        return itemService.searchByName(name);
    }
}