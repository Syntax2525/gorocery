package com.pickncart;

import com.pickncart.model.Cart;
import com.pickncart.model.Item;
import com.pickncart.model.User;
import com.pickncart.repository.CartRepository;
import com.pickncart.service.CartService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartServiceTest {

    private final CartRepository cartRepository = mock(CartRepository.class);
    private final CartService cartService = new CartService(cartRepository);

    @Test
    void adminCannotAddItemsToCart() {
        User admin = user("ADMIN");
        Item item = item();

        assertThatThrownBy(() -> cartService.addOrUpdateCart(admin, item, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Admins cannot add items to cart");
    }

    @Test
    void customerCanAddItemsToCart() {
        User customer = user("CUSTOMER");
        Item item = item();
        Cart saved = new Cart();
        saved.setUser(customer);
        saved.setItem(item);
        saved.setQuantity(2);

        when(cartRepository.findByUserAndItem(customer, item)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        Cart cart = cartService.addOrUpdateCart(customer, item, 2);

        assertThat(cart.getQuantity()).isEqualTo(2);
        verify(cartRepository).save(any(Cart.class));
    }

    private User user(String role) {
        User user = new User();
        user.setRole(role);
        return user;
    }

    private Item item() {
        Item item = new Item();
        item.setPrice(BigDecimal.valueOf(1000));
        return item;
    }
}
