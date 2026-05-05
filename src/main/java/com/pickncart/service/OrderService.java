package com.pickncart.service;

import com.pickncart.model.Order;
import com.pickncart.model.OrderItem;
import com.pickncart.model.User;
import com.pickncart.repository.OrderRepository;
import com.pickncart.util.RoleUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryFeeService deliveryFeeService;

    public OrderService(OrderRepository orderRepository, DeliveryFeeService deliveryFeeService) {
        this.orderRepository = orderRepository;
        this.deliveryFeeService = deliveryFeeService;
    }

    public Order placeOrder(Order order, User user) {
        if (RoleUtils.isAdmin(user)) {
            throw new IllegalStateException("Admins cannot place customer orders");
        }
        order.setUser(user);
        order.setStatus("PLACED");
        order.setCreatedAt(java.time.LocalDateTime.now());

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(oi -> {
                oi.setOrder(order);
                if (oi.getPrice() == null && oi.getItem() != null) {
                    oi.setPrice(oi.getItem().getPrice());
                }
            });
        }

        BigDecimal subtotal = calculateSubtotal(order.getOrderItems());
        BigDecimal deliveryFee = deliveryFeeService.calculateForOrderItems(order.getOrderItems(), resolveDistrict(order, user));
        order.setSubtotalAmount(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(subtotal.add(deliveryFee));

        if (order.getDeliveryDistrict() == null || order.getDeliveryDistrict().isBlank()) {
            order.setDeliveryDistrict(user != null ? user.getDistrict() : null);
        }

        return orderRepository.save(order);
    }

    public Order reorder(Long orderId, User user) {
        if (RoleUtils.isAdmin(user)) {
            throw new IllegalStateException("Admins cannot place customer orders");
        }
        Optional<Order> existing = orderRepository.findById(orderId);
        if (existing.isEmpty()) {
            return null;
        }

        Order original = existing.get();
        Order order = new Order();
        order.setUser(user);
        order.setStatus("PLACED");
        order.setAddress(original.getAddress());
        order.setPhone(original.getPhone());
        order.setDeliveryDistrict(original.getDeliveryDistrict());
        order.setPaymentMethod(original.getPaymentMethod());

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

        BigDecimal subtotal = calculateSubtotal(order.getOrderItems());
        BigDecimal deliveryFee = deliveryFeeService.calculateForOrderItems(order.getOrderItems(), resolveDistrict(order, user));
        order.setSubtotalAmount(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(subtotal.add(deliveryFee));

        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
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

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    private BigDecimal calculateSubtotal(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return BigDecimal.ZERO;
        }
        return orderItems.stream()
                .filter(oi -> oi.getPrice() != null)
                .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String resolveDistrict(Order order, User user) {
        if (order != null && order.getDeliveryDistrict() != null && !order.getDeliveryDistrict().isBlank()) {
            return order.getDeliveryDistrict();
        }
        return user == null ? null : user.getDistrict();
    }
}
