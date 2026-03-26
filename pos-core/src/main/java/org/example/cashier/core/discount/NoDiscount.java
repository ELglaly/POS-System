package org.example.cashier.core.discount;

import java.math.BigDecimal;

public class NoDiscount implements DiscountStrategy {
    @Override
    public BigDecimal calculate(BigDecimal subtotal, int itemCount) {
        return BigDecimal.ZERO;
    }
}
