package org.example.cashier.services;

import javafx.collections.ObservableList;
import org.example.cashier.core.discount.DiscountStrategy;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.model.CartItem;

import java.math.BigDecimal;

/**
 * Manages the active shopping cart for a single POS session.
 * The list returned by {@link #getItems()} is observable so that JavaFX
 * TableViews bound to it update automatically on every mutation.
 */
public interface CartService {

    /** Add one unit of a product, respecting stock limits. */
    void addItem(Product product);

    /** Add a specific quantity of a product, respecting stock limits. */
    void addItem(Product product, int requestedQty);

    /** Remove all units of a product from the cart. */
    void removeItem(Long productId);

    /** Remove every item from the cart. */
    void clear();

    /** Replace the active discount strategy. Pass {@code NoDiscount} to clear. */
    void setDiscountStrategy(DiscountStrategy strategy);

    /** Observable list of cart items — safe to bind directly to a JavaFX TableView. */
    ObservableList<CartItem> getItems();

    boolean isEmpty();

    BigDecimal getSubtotal();

    BigDecimal getDiscountAmount();

    BigDecimal getTotal();
}
