package com.pickncart.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Order> existing = orderService.getById(id);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (!isAdmin(authentication) && existing.get().getUser() != null
                && !existing.get().getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot reorder another user's order");
        }
        return orderService.reorder(id, currentUser);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
}
