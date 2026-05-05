package com.pickncart;

import com.pickncart.model.Order;
import com.pickncart.model.User;
import com.pickncart.repository.OrderRepository;
import com.pickncart.service.DeliveryFeeService;
import com.pickncart.service.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderService orderService = new OrderService(orderRepository, new DeliveryFeeService());

    @Test
    void adminCannotPlaceOrder() {
        User admin = user("ROLE_ADMIN");

        assertThatThrownBy(() -> orderService.placeOrder(new Order(), admin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Admins cannot place customer orders");
    }

    @Test
    void customerCanPlaceOrder() {
        User customer = user("CUSTOMER");
        Order saved = new Order();
        saved.setUser(customer);
        saved.setStatus("PLACED");
        saved.setTotalAmount(BigDecimal.ZERO);

        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        Order order = orderService.placeOrder(new Order(), customer);

        assertThat(order.getUser()).isEqualTo(customer);
        assertThat(order.getStatus()).isEqualTo("PLACED");
        verify(orderRepository).save(any(Order.class));
    }

    private User user(String role) {
        User user = new User();
        user.setRole(role);
        return user;
    }
}
