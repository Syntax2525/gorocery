package com.pickncart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pickncart.model.Cart;
import com.pickncart.model.User;
import com.pickncart.model.Item;
import com.pickncart.repository.CartRepository;
import com.pickncart.util.RoleUtils;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart addToCart(Cart cart) {
        if (cart != null && RoleUtils.isAdmin(cart.getUser())) {
            throw new IllegalStateException("Admins cannot add items to cart");
        }
        return cartRepository.save(cart);
    }

    public Cart addOrUpdateCart(User user, Item item, int quantity) {
        if (RoleUtils.isAdmin(user)) {
            throw new IllegalStateException("Admins cannot add items to cart");
        }
        var existing = cartRepository.findByUserAndItem(user, item);
        if (existing.isPresent()) {
            Cart cart = existing.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            return cartRepository.save(cart);
        } else {
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setItem(item);
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }
    }

    public List<Cart> getUserCart(User user) {
        return cartRepository.findByUser(user);
    }

    public void removeFromCart(Long id) {
        cartRepository.deleteById(id);
    }

    public void removeFromCart(User user, Long itemId) {
        cartRepository.deleteByUserAndItem(user, cartRepository.findById(itemId).orElseThrow().getItem());
    }

    public Cart getById(Long id) {
        return cartRepository.findById(id).orElse(null);
    }

    public int getItemCount(User user) {
        return getUserCart(user).stream().mapToInt(Cart::getQuantity).sum();
    }

    public double getTotal(User user) {
        return getUserCart(user).stream()
                .mapToDouble(cart -> cart.getItem().getPrice().doubleValue() * cart.getQuantity())
                .sum();
    }

    public List<Cart> getCartItems(User user) {
        return getUserCart(user);
    }

    public void updateQuantity(Long cartId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (RoleUtils.isAdmin(cart.getUser())) {
            throw new IllegalStateException("Admins cannot update cart items");
        }
        if (quantity <= 0) {
            cartRepository.delete(cart);
        } else {
            cart.setQuantity(quantity);
            cartRepository.save(cart);
        }
    }

    public void clearCart(User user) {
        List<Cart> items = cartRepository.findByUser(user);
        cartRepository.deleteAll(items);
    }
}
