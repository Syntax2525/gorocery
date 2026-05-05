package com.pickncart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pickncart.model.Cart;
import com.pickncart.model.User;
import com.pickncart.model.Item;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User user);
    boolean existsByItemId(Long itemId);
    Optional<Cart> findByUserAndItem(User user, Item item);
    void deleteByUserAndItem(User user, Item item);
}
