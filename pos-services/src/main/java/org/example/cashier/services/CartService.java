package org.example.cashier.services;

import javafx.collections.ObservableList;
import org.example.cashier.core.discount.DiscountStrategy;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.model.CartItem;

import java.math.BigDecimal;

public interface CartService {

    void addItem(Product product);

    void addItem(Product product, int requestedQty);

    void removeItem(Long productId);

    void clear();
    void setDiscountStrategy(DiscountStrategy strategy);

    ObservableList<CartItem> getItems();

    boolean isEmpty();

    BigDecimal getSubtotal();

    BigDecimal getDiscountAmount();

    BigDecimal getTotal();
}
