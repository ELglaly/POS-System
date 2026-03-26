package org.example.cashier.services;

import lombok.RequiredArgsConstructor;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.entity.Transaction;
import org.example.cashier.core.entity.TransactionItem;
import org.example.cashier.core.entity.User;
import org.example.cashier.core.model.CartItem;
import org.example.cashier.data.repository.ProductRepository;
import org.example.cashier.data.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Atomic checkout: validates stock, reduces inventory, persists the Transaction.
 * Rolls back everything if any step fails.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService           cartService;
    private final TransactionRepository transactionRepository;
    private final ProductRepository     productRepository;

    @Transactional
    public Transaction checkout(User cashier,
                                Transaction.PaymentMethod paymentMethod,
                                BigDecimal amountTendered) {

        if (cartService.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        BigDecimal total = cartService.getTotal();

        if (paymentMethod == Transaction.PaymentMethod.CASH) {
            if (amountTendered == null || amountTendered.compareTo(total) < 0) {
                throw new IllegalArgumentException(
                        "Amount tendered (" + amountTendered +
                        ") is less than total (" + total + ").");
            }
        }

        Transaction sale = new Transaction();
        sale.setCashier(cashier);
        sale.setSubtotal(cartService.getSubtotal());
        sale.setDiscountAmount(cartService.getDiscountAmount());
        sale.setTotal(total);
        sale.setPaymentMethod(paymentMethod);
        sale.setAmountTendered(amountTendered);
        sale.setChangeGiven(
                paymentMethod == Transaction.PaymentMethod.CASH
                        ? amountTendered.subtract(total)
                        : BigDecimal.ZERO);

        for (CartItem cartItem : cartService.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Product not found: " + cartItem.getProduct().getName()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for \"" + product.getName() +
                        "\" (available: " + product.getStockQuantity() + ").");
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
            sale.addItem(new TransactionItem(product, cartItem.getQuantity()));
        }

        Transaction saved = transactionRepository.save(sale);
        cartService.clear();
        return saved;
    }
}
