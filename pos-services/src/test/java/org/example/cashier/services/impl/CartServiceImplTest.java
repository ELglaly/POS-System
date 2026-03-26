package org.example.cashier.services.impl;

import org.example.cashier.core.discount.NoDiscount;
import org.example.cashier.core.discount.PercentageDiscount;
import org.example.cashier.core.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CartServiceImpl")
class CartServiceImplTest {

    private CartServiceImpl cart;

    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        cart = new CartServiceImpl();

        productA = Product.builder()
                .id(1L).name("Apple").sku("P-AAA001")
                .price(new BigDecimal("2.50")).stockQuantity(20)
                .active(true).build();

        productB = Product.builder()
                .id(2L).name("Banana").sku("P-BBB002")
                .price(new BigDecimal("1.00")).stockQuantity(5)
                .active(true).build();
    }

    // ── addItem ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("adds a new product as a single cart line")
        void addsNewProduct() {
            cart.addItem(productA);

            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getProduct()).isEqualTo(productA);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("increments quantity when the same product is added again")
        void incrementsExistingLine() {
            cart.addItem(productA);
            cart.addItem(productA);

            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("adds a specific quantity in one call")
        void addsSpecificQuantity() {
            cart.addItem(productA, 3);

            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("multiple products appear as separate lines")
        void multipleProductLines() {
            cart.addItem(productA);
            cart.addItem(productB);

            assertThat(cart.getItems()).hasSize(2);
        }

        @Test
        @DisplayName("throws IllegalStateException when requested quantity exceeds stock")
        void stockEnforced() {
            assertThatThrownBy(() -> cart.addItem(productB, 10))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough stock");
        }

        @Test
        @DisplayName("throws when cart quantity + new quantity would exceed stock")
        void stockEnforcedCumulatively() {
            cart.addItem(productB, 4);   // stock = 5, now 4 in cart

            assertThatThrownBy(() -> cart.addItem(productB, 2))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("adding exactly the available stock succeeds")
        void addExactStock() {
            cart.addItem(productB, 5);   // stock = 5

            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        }
    }

    // ── removeItem ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeItem")
    class RemoveItem {

        @Test
        @DisplayName("removes the matching product line entirely")
        void removesLine() {
            cart.addItem(productA);
            cart.addItem(productB);
            cart.removeItem(productA.getId());

            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getProduct()).isEqualTo(productB);
        }

        @Test
        @DisplayName("removing a non-existent product is a no-op")
        void removeNonExistentIsNoOp() {
            cart.addItem(productA);
            cart.removeItem(999L);

            assertThat(cart.getItems()).hasSize(1);
        }
    }

    // ── clear ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("clear() removes all items and cart becomes empty")
    void clear() {
        cart.addItem(productA);
        cart.addItem(productB);
        cart.clear();

        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getItems()).isEmpty();
    }

    // ── totals ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("totals")
    class Totals {

        @Test
        @DisplayName("subtotal = sum of (price × qty) for all lines")
        void subtotal() {
            cart.addItem(productA, 2);  // 2 × 2.50 = 5.00
            cart.addItem(productB, 3);  // 3 × 1.00 = 3.00

            assertThat(cart.getSubtotal()).isEqualByComparingTo("8.00");
        }

        @Test
        @DisplayName("discount amount is zero when NoDiscount strategy is active")
        void noDiscount() {
            cart.addItem(productA, 2);

            assertThat(cart.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("discount amount reflects percentage strategy")
        void percentageDiscount() {
            cart.addItem(productA, 4);  // subtotal = 10.00
            cart.setDiscountStrategy(new PercentageDiscount(10));

            assertThat(cart.getDiscountAmount()).isEqualByComparingTo("1.00");
        }

        @Test
        @DisplayName("total = subtotal - discountAmount")
        void totalWithDiscount() {
            cart.addItem(productA, 4);  // subtotal = 10.00
            cart.setDiscountStrategy(new PercentageDiscount(10)); // discount = 1.00

            assertThat(cart.getTotal()).isEqualByComparingTo("9.00");
        }

        @Test
        @DisplayName("total is zero when 100% discount applied")
        void totalNeverNegative() {
            cart.addItem(productA, 1);
            cart.setDiscountStrategy(new PercentageDiscount(100));

            assertThat(cart.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("isEmpty returns true on a new cart")
        void emptyOnNew() {
            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty returns false after adding an item")
        void notEmptyAfterAdd() {
            cart.addItem(productA);

            assertThat(cart.isEmpty()).isFalse();
        }
    }

    // ── discount strategy swap ────────────────────────────────────────────

    @Test
    @DisplayName("switching back to NoDiscount after PercentageDiscount zeroes the discount")
    void switchDiscountStrategy() {
        cart.addItem(productA, 4);  // subtotal = 10.00
        cart.setDiscountStrategy(new PercentageDiscount(20));
        assertThat(cart.getDiscountAmount()).isEqualByComparingTo("2.00");

        cart.setDiscountStrategy(new NoDiscount());
        assertThat(cart.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
