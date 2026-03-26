package org.example.cashier.services;

import org.example.cashier.core.entity.Transaction;
import org.example.cashier.core.entity.User;

import java.math.BigDecimal;

/**
 * Executes the checkout process: validates the cart, deducts stock,
 * persists the transaction, and clears the cart.
 */
public interface CheckoutService {

    /**
     * Complete the sale for the current cart.
     *
     * @param cashier         the logged-in operator
     * @param paymentMethod   CASH or CARD
     * @param amountTendered  cash handed over (required for CASH, ignored for CARD)
     * @return the persisted {@link Transaction}
     * @throws IllegalStateException     if the cart is empty or stock is insufficient
     * @throws IllegalArgumentException  if cash tendered is less than the total
     */
    Transaction checkout(User cashier,
                         Transaction.PaymentMethod paymentMethod,
                         BigDecimal amountTendered);
}
