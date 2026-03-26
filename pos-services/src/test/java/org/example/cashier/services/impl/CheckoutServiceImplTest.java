package org.example.cashier.services.impl;

import org.example.cashier.core.entity.Product;
import org.example.cashier.core.entity.Transaction;
import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;
import org.example.cashier.core.model.CartItem;
import org.example.cashier.data.repository.ProductRepository;
import org.example.cashier.data.repository.TransactionRepository;
import org.example.cashier.services.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutServiceImpl")
class CheckoutServiceImplTest {

    @Mock CartService           cartService;
    @Mock TransactionRepository transactionRepository;
    @Mock ProductRepository     productRepository;

    @InjectMocks CheckoutServiceImpl checkoutService;

    private User cashier;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cashier = User.builder()
                .id(1L).username("cashier").role(UserRole.CASHIER).active(true).build();

        product = Product.builder()
                .id(10L).name("Widget").sku("P-WID001")
                .price(new BigDecimal("25.00")).stockQuantity(10)
                .active(true).build();

        cartItem = new CartItem(product, 2);

        // Default cart: 2 × Widget = 50.00, no discount
        when(cartService.isEmpty()).thenReturn(false);
        when(cartService.getItems()).thenReturn(
                javafx.collections.FXCollections.observableArrayList(cartItem));
        when(cartService.getSubtotal()).thenReturn(new BigDecimal("50.00"));
        when(cartService.getDiscountAmount()).thenReturn(BigDecimal.ZERO);
        when(cartService.getTotal()).thenReturn(new BigDecimal("50.00"));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── Happy path ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("successful checkout")
    class SuccessfulCheckout {

        @Test
        @DisplayName("persists a COMPLETED transaction with correct totals")
        void persistsTransaction() {
            checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                    new BigDecimal("60.00"));

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            Transaction tx = captor.getValue();

            assertThat(tx.getTotal()).isEqualByComparingTo("50.00");
            assertThat(tx.getSubtotal()).isEqualByComparingTo("50.00");
            assertThat(tx.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(tx.getCashier()).isEqualTo(cashier);
            assertThat(tx.getPaymentMethod()).isEqualTo(Transaction.PaymentMethod.CASH);
        }

        @Test
        @DisplayName("calculates correct change for cash payment")
        void calculatesChange() {
            checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                    new BigDecimal("60.00"));

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            assertThat(captor.getValue().getChangeGiven()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("change is zero for card payment")
        void cardPaymentZeroChange() {
            checkoutService.checkout(cashier, Transaction.PaymentMethod.CARD, null);

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            assertThat(captor.getValue().getChangeGiven()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("deducts stock quantity from product")
        void deductsStock() {
            checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                    new BigDecimal("50.00"));

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            assertThat(captor.getValue().getStockQuantity()).isEqualTo(8); // 10 - 2
        }

        @Test
        @DisplayName("clears the cart after a successful checkout")
        void clearsCart() {
            checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                    new BigDecimal("50.00"));

            verify(cartService).clear();
        }

        @Test
        @DisplayName("exact-change cash payment succeeds")
        void exactChangeCash() {
            checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                    new BigDecimal("50.00"));

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            assertThat(captor.getValue().getChangeGiven()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── Validation guards ─────────────────────────────────────────────────

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("throws IllegalStateException when cart is empty")
        void emptyCartThrows() {
            when(cartService.isEmpty()).thenReturn(true);

            assertThatThrownBy(() ->
                    checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                            new BigDecimal("100.00")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when cash tendered is less than total")
        void insufficientCashThrows() {
            assertThatThrownBy(() ->
                    checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                            new BigDecimal("40.00")))  // total = 50.00
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("less than total");
        }

        @Test
        @DisplayName("throws IllegalStateException when product has insufficient stock at checkout time")
        void insufficientStockThrows() {
            product.setStockQuantity(1); // cart wants 2, only 1 in stock now

            assertThatThrownBy(() ->
                    checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                            new BigDecimal("50.00")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("throws IllegalStateException when product not found at checkout time")
        void productNotFoundThrows() {
            when(productRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    checkoutService.checkout(cashier, Transaction.PaymentMethod.CASH,
                            new BigDecimal("50.00")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
