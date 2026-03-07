package com.pickncart.service;

import org.springframework.stereotype.Service;
import com.pickncart.repository.CartRepository;
import com.pickncart.model.Cart;
import com.pickncart.model.User;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart addToCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public List<Cart> getUserCart(User user) {
        return cartRepository.findByUser(user);
    }

    public void removeFromCart(Long id) {
        cartRepository.deleteById(id);
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
}