package org.example.cashier.core.model;

import org.example.cashier.core.entity.Product;

import java.math.BigDecimal;

/**
 * In-memory cart line — NOT a JPA entity.
 * Discarded when the cart is cleared or the app restarts.
 */
public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product  = product;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Product getProduct()          { return product; }
    public int     getQuantity()         { return quantity; }
    public void    setQuantity(int qty)  { this.quantity = qty; }
    public void    incrementQuantity()   { this.quantity++; }
}
