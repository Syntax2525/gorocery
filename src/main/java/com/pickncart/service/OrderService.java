package com.pickncart.service;

import com.pickncart.model.Order;
import com.pickncart.model.OrderItem;
import com.pickncart.model.User;
import com.pickncart.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order order, User user) {
        order.setUser(user);
        order.setStatus("PLACED");

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(oi -> {
                oi.setOrder(order);
                if (oi.getPrice() == null && oi.getItem() != null) {
                    oi.setPrice(oi.getItem().getPrice());
                }
            });
        }

        if (order.getTotalAmount() == null) {
            BigDecimal total = BigDecimal.ZERO;
            if (order.getOrderItems() != null) {
                total = order.getOrderItems().stream()
                        .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            order.setTotalAmount(total);
        }

        return orderRepository.save(order);
    }

    public Order reorder(Long orderId, User user) {
        Optional<Order> existing = orderRepository.findById(orderId);
        if (existing.isEmpty()) {
            return null;
        }

        Order original = existing.get();
        Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");

        if (original.getOrderItems() != null) {
            List<OrderItem> orderItems = original.getOrderItems().stream().map(oi -> {
                OrderItem copy = new OrderItem();
                copy.setItem(oi.getItem());
                copy.setQuantity(oi.getQuantity());
                copy.setPrice(oi.getPrice());
                copy.setOrder(order);
                return copy;
            }).collect(Collectors.toList());
            order.setOrderItems(orderItems);
        }

        BigDecimal total = order.getOrderItems() == null ? BigDecimal.ZERO : order.getOrderItems().stream()
                .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUser(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getById(Long id) {
        return orderRepository.findById(id);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public boolean requestCancel(Long orderId, User user) {
        Optional<Order> existing = orderRepository.findById(orderId);
        if (existing.isEmpty()) {
            return false;
        }
        Order order = existing.get();
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            return false;
        }
        if ("CANCELLED".equals(order.getStatus())) {
            return true;
        }
        order.setStatus("CANCEL_REQUESTED");
        orderRepository.save(order);
        return true;
    }

    public boolean approveCancel(Long orderId) {
        Optional<Order> existing = orderRepository.findById(orderId);
        if (existing.isEmpty()) {
            return false;
        }
        orderRepository.delete(existing.get());
        return true;
    }
}
