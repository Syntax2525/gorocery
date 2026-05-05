package com.pickncart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pickncart.model.Item;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByNameContainingIgnoreCase(String name);
    List<Item> findByCategoryId(Long categoryId);
    List<Item> findByCategoryIdAndStockGreaterThan(Long categoryId, int stock);
    List<Item> findByStockGreaterThan(int stock);
}
