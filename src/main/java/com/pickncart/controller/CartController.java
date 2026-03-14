package com.pickncart.controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.pickncart.service.CartService;
import com.pickncart.service.UserService;
import com.pickncart.model.Cart;
import com.pickncart.model.User;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
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

    @PostMapping
    public Cart add(@RequestBody Cart cart) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            cart.setUser(currentUser);
        }
        return cartService.addToCart(cart);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        cartService.removeFromCart(id);
    }
}