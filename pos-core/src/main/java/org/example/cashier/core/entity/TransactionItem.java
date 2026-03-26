package org.example.cashier.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "transaction_item", indexes = {
        @Index(name = "idx_txn_item_transaction", columnList = "transaction_id")
})
@Data
@NoArgsConstructor
public class TransactionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Snapshot of the product name at time of sale. */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /** Snapshot of unit price at time of sale. */
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal subtotal;

    public TransactionItem(Product product, int quantity) {
        this.product     = product;
        this.productName = product.getName();
        this.unitPrice   = product.getPrice();
        this.quantity    = quantity;
        this.subtotal    = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
