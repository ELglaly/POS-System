package org.example.cashier.services.impl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.cashier.core.discount.DiscountStrategy;
import org.example.cashier.core.discount.NoDiscount;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.model.CartItem;
import org.example.cashier.services.CartService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final ObservableList<CartItem> items = FXCollections.observableArrayList();
    private DiscountStrategy discountStrategy = new NoDiscount();

    @Override
    public void addItem(Product product) {
        addItem(product, 1);
    }

    @Override
    public void addItem(Product product, int requestedQty) {
        Optional<CartItem> existing = findByProductId(product.getId());
        int currentInCart  = existing.map(CartItem::getQuantity).orElse(0);
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
            items.set(items.indexOf(item), item);
        } else {
            items.add(new CartItem(product, requestedQty));
        }
    }

    @Override
    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public void setDiscountStrategy(DiscountStrategy strategy) {
        this.discountStrategy = strategy;
    }

    @Override
    public ObservableList<CartItem> getItems() { return items; }

    @Override
    public boolean isEmpty() { return items.isEmpty(); }

    @Override
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getDiscountAmount() {
        return discountStrategy
                .calculate(getSubtotal(), items.size())
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
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
