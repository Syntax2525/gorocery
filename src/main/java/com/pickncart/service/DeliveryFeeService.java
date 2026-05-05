package com.pickncart.service;

import com.pickncart.model.Cart;
import com.pickncart.model.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryFeeService {

    private static final BigDecimal EXTRA_ITEM_FEE = BigDecimal.valueOf(700);
    private static final BigDecimal MID_VALUE_FEE = BigDecimal.valueOf(1500);
    private static final BigDecimal HIGH_VALUE_FEE = BigDecimal.valueOf(3000);

    public BigDecimal calculateForCart(List<Cart> cartItems, String district) {
        BigDecimal subtotal = cartItems == null ? BigDecimal.ZERO : cartItems.stream()
                .filter(cart -> cart.getItem() != null && cart.getItem().getPrice() != null)
                .map(cart -> cart.getItem().getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = cartItems == null ? 0 : cartItems.stream().mapToInt(Cart::getQuantity).sum();
        return calculate(subtotal, itemCount, district);
    }

    public BigDecimal calculateForOrderItems(List<OrderItem> orderItems, String district) {
        BigDecimal subtotal = orderItems == null ? BigDecimal.ZERO : orderItems.stream()
                .filter(orderItem -> orderItem.getPrice() != null)
                .map(orderItem -> orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = orderItems == null ? 0 : orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
        return calculate(subtotal, itemCount, district);
    }

    public Map<String, BigDecimal> calculateDistrictOptionsForCart(List<Cart> cartItems) {
        Map<String, BigDecimal> fees = new LinkedHashMap<>();
        districtBaseFees().keySet().forEach(district -> fees.put(district, calculateForCart(cartItems, district)));
        return fees;
    }

    public BigDecimal calculate(BigDecimal subtotal, int itemCount, String district) {
        BigDecimal fee = baseFeeForDistrict(district);
        int extraItems = Math.max(0, itemCount - 1);
        fee = fee.add(EXTRA_ITEM_FEE.multiply(BigDecimal.valueOf(extraItems)));

        if (subtotal != null && subtotal.compareTo(BigDecimal.valueOf(200000)) >= 0) {
            fee = fee.add(HIGH_VALUE_FEE);
        } else if (subtotal != null && subtotal.compareTo(BigDecimal.valueOf(100000)) >= 0) {
            fee = fee.add(MID_VALUE_FEE);
        }

        return fee;
    }

    private BigDecimal baseFeeForDistrict(String district) {
        return districtBaseFees().getOrDefault(normalizeDistrict(district), BigDecimal.valueOf(6000));
    }

    private Map<String, BigDecimal> districtBaseFees() {
        Map<String, BigDecimal> fees = new LinkedHashMap<>();
        fees.put("Ilala", BigDecimal.valueOf(3500));
        fees.put("Kinondoni", BigDecimal.valueOf(4500));
        fees.put("Ubungo", BigDecimal.valueOf(5500));
        fees.put("Temeke", BigDecimal.valueOf(6500));
        fees.put("Kigamboni", BigDecimal.valueOf(8000));
        return fees;
    }

    private String normalizeDistrict(String district) {
        if (district == null || district.isBlank()) {
            return "";
        }
        return district.trim();
    }
}
