package org.example.cashier.core.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageDiscount implements DiscountStrategy {

    private final double percentage;

    public PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public BigDecimal calculate(BigDecimal subtotal, int itemCount) {
        return subtotal.multiply(BigDecimal.valueOf(percentage / 100.0))
                       .setScale(2, RoundingMode.HALF_UP);
    }
}
