package org.example.cashier.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.cashier.core.discount.DiscountStrategy;
import org.example.cashier.core.discount.NoDiscount;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.model.CartItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * In-memory cart state — singleton per application session.
 * The ObservableList means any bound TableView refreshes automatically.
 */
@Service
public class CartService {

    private final ObservableList<CartItem> items = FXCollections.observableArrayList();
    private DiscountStrategy discountStrategy = new NoDiscount();

    // ── Mutations ──────────────────────────────────────────────────────────

    public void addItem(Product product) {
        addItem(product, 1);
    }

    public void addItem(Product product, int requestedQty) {
        Optional<CartItem> existing = findByProductId(product.getId());
        int currentInCart = existing.map(CartItem::getQuantity).orElse(0);
        int totalRequested = currentInCart + requestedQty;

        if (totalRequested > product.getStockQuantity()) {
            throw new IllegalStateException(
                    "Not enough stock for \"" + product.getName() + "\". " +
                    "Available: " + product.getStockQuantity() +
                    ", Already in cart: " + currentInCart);
        }

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + requestedQty);
            int idx = items.indexOf(item);
            items.set(idx, item);
        } else {
            items.add(new CartItem(product, requestedQty));
        }
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clear() {
        items.clear();
    }

    // ── Discount ───────────────────────────────────────────────────────────

    public void setDiscountStrategy(DiscountStrategy strategy) {
        this.discountStrategy = strategy;
    }

    public void clearDiscount() {
        this.discountStrategy = new NoDiscount();
    }

    // ── Queries ────────────────────────────────────────────────────────────

    public ObservableList<CartItem> getItems() { return items; }

    public boolean isEmpty() { return items.isEmpty(); }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDiscountAmount() {
        return discountStrategy
                .calculate(getSubtotal(), items.size())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return getSubtotal()
                .subtract(getDiscountAmount())
                .max(BigDecimal.ZERO);
    }

    private Optional<CartItem> findByProductId(Long productId) {
        return items.stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();
    }
}
