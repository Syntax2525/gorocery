package com.pickncart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pickncart.model.Cart;
import com.pickncart.model.User;
import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUser(User user);

    boolean existsByItemId(Long itemId);

}
