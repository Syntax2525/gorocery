package com.pickncart;

import com.pickncart.service.DeliveryFeeService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryFeeServiceTest {

    private final DeliveryFeeService deliveryFeeService = new DeliveryFeeService();

    @Test
    void feeVariesByDistrictDistance() {
        BigDecimal ilalaFee = deliveryFeeService.calculate(BigDecimal.valueOf(50000), 1, "Ilala");
        BigDecimal kigamboniFee = deliveryFeeService.calculate(BigDecimal.valueOf(50000), 1, "Kigamboni");

        assertThat(kigamboniFee).isGreaterThan(ilalaFee);
    }

    @Test
    void feeVariesByItemsBought() {
        BigDecimal oneItemFee = deliveryFeeService.calculate(BigDecimal.valueOf(50000), 1, "Kinondoni");
        BigDecimal fourItemFee = deliveryFeeService.calculate(BigDecimal.valueOf(50000), 4, "Kinondoni");

        assertThat(fourItemFee).isGreaterThan(oneItemFee);
    }
}
