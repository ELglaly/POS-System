package org.example.cashier.core.discount;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DiscountStrategy")
class DiscountStrategyTest {

    // ── NoDiscount ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("NoDiscount")
    class NoDiscountTests {

        private final DiscountStrategy strategy = new NoDiscount();

        @Test
        @DisplayName("always returns zero regardless of subtotal")
        void alwaysReturnsZero() {
            assertThat(strategy.calculate(new BigDecimal("150.00"), 5))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("returns zero for a zero subtotal")
        void zeroSubtotal() {
            assertThat(strategy.calculate(BigDecimal.ZERO, 0))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("returns zero for a large subtotal")
        void largeSubtotal() {
            assertThat(strategy.calculate(new BigDecimal("9999999.99"), 100))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── PercentageDiscount ────────────────────────────────────────────────

    @Nested
    @DisplayName("PercentageDiscount")
    class PercentageDiscountTests {

        @Test
        @DisplayName("10% of 200.00 = 20.00")
        void tenPercent() {
            DiscountStrategy strategy = new PercentageDiscount(10.0);
            assertThat(strategy.calculate(new BigDecimal("200.00"), 3))
                    .isEqualByComparingTo(new BigDecimal("20.00"));
        }

        @Test
        @DisplayName("0% returns zero")
        void zeroPercent() {
            DiscountStrategy strategy = new PercentageDiscount(0.0);
            assertThat(strategy.calculate(new BigDecimal("100.00"), 1))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("100% discount equals full subtotal")
        void fullDiscount() {
            DiscountStrategy strategy = new PercentageDiscount(100.0);
            assertThat(strategy.calculate(new BigDecimal("50.00"), 2))
                    .isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("result is rounded to 2 decimal places (HALF_UP)")
        void rounding() {
            // 15% of 33.33 = 4.9995 → rounds to 5.00
            DiscountStrategy strategy = new PercentageDiscount(15.0);
            assertThat(strategy.calculate(new BigDecimal("33.33"), 1))
                    .isEqualByComparingTo(new BigDecimal("5.00"));
        }

        @ParameterizedTest(name = "{0}% of {1} = {2}")
        @CsvSource({
                "5,   100.00, 5.00",
                "25,  80.00,  20.00",
                "50,  60.00,  30.00",
                "7.5, 200.00, 15.00",
        })
        @DisplayName("parameterised percentage calculations")
        void parameterised(double pct, String subtotal, String expected) {
            DiscountStrategy strategy = new PercentageDiscount(pct);
            assertThat(strategy.calculate(new BigDecimal(subtotal), 1))
                    .isEqualByComparingTo(new BigDecimal(expected));
        }
    }
}
