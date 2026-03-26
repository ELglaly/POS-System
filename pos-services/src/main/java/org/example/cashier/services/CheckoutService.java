package org.example.cashier.services;

import org.example.cashier.core.entity.Transaction;
import org.example.cashier.core.entity.User;

import java.math.BigDecimal;

public interface CheckoutService {

    Transaction checkout(User cashier,
                         Transaction.PaymentMethod paymentMethod,
                         BigDecimal amountTendered);
}
