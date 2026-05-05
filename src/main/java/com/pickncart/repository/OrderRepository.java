package com.pickncart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pickncart.model.Order;
import com.pickncart.model.User;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByStatus(String status);
    List<Order> findAllByOrderByCreatedAtDesc();
}
