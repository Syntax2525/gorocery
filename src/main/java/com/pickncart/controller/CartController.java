package com.pickncart.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.pickncart.service.CartService;
import com.pickncart.service.UserService;
import com.pickncart.service.ItemService;
import com.pickncart.model.Cart;
import com.pickncart.model.User;
import com.pickncart.model.Item;
import com.pickncart.util.RoleUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ItemService itemService;

    public CartController(CartService cartService, UserService userService, ItemService itemService) {
        this.cartService = cartService;
        this.userService = userService;
        this.itemService = itemService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Optional<User> user = userService.findByEmail(authentication.getName());
            return user.orElse(null);
        }
        return null;
    }

    @GetMapping
    public List<Cart> getCart() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        return cartService.getCartItems(currentUser);
    }

    @PostMapping
    public Cart add(@RequestBody CartRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        if (RoleUtils.isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins cannot add items to cart");
        }
        Item item = itemService.getById(request.getItemId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        return cartService.addOrUpdateCart(currentUser, item, request.getQuantity());
    }

    @PutMapping("/{id}")
    public Cart updateQuantity(@PathVariable Long id, @RequestBody CartRequest request) {
        cartService.updateQuantity(id, request.getQuantity());
        return cartService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        cartService.removeFromCart(id);
    }

    @DeleteMapping
    public void clear() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            cartService.clearCart(currentUser);
        }
    }

    @GetMapping("/total")
    public double getTotal() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return 0.0;
        }
        return cartService.getTotal(currentUser);
    }

    public static class CartRequest {
        private Long itemId;
        private int quantity;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
