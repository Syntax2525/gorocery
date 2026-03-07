package com.pickncart.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.pickncart.service.OrderService;
import com.pickncart.service.UserService;
import com.pickncart.model.Order;
import com.pickncart.model.User;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Optional<User> user = userService.findByEmail(authentication.getName());
            return user.orElse(null);
        }
        return null;
    }

    @PostMapping("/create")
    public Order placeOrder(@RequestBody Order order) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return orderService.placeOrder(order, currentUser);
    }

    @GetMapping("/user")
    public List<Order> getUserOrders() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        return orderService.getUserOrders(currentUser);
    }

    @PostMapping("/reorder/{id}")
    public Order reorder(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return orderService.reorder(id, currentUser);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
}