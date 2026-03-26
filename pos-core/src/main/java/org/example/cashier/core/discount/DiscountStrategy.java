package org.example.cashier.core.discount;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal calculate(BigDecimal subtotal, int itemCount);
}
