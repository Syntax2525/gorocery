package com.pickncart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pickncart.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByItemId(Long itemId);

}
