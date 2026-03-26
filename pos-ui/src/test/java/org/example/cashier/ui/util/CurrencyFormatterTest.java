package org.example.cashier.ui.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CurrencyFormatter")
class CurrencyFormatterTest {

    private static final String DEFAULT_SYMBOL = " EGY ";

    @BeforeEach
    void resetSymbol() {
        CurrencyFormatter.setSymbol(DEFAULT_SYMBOL);
    }

    @AfterEach
    void restoreSymbol() {
        CurrencyFormatter.setSymbol(DEFAULT_SYMBOL);
    }

    @Test
    @DisplayName("formats a positive amount with two decimal places")
    void formatsPositiveAmount() {
        assertThat(CurrencyFormatter.format(new BigDecimal("49.9")))
                .isEqualTo(DEFAULT_SYMBOL + "49.90");
    }

    @Test
    @DisplayName("formats zero")
    void formatsZero() {
        assertThat(CurrencyFormatter.format(BigDecimal.ZERO))
                .isEqualTo(DEFAULT_SYMBOL + "0.00");
    }

    @Test
    @DisplayName("returns symbol + 0.00 for null amount")
    void nullAmountReturnsZero() {
        assertThat(CurrencyFormatter.format((BigDecimal) null))
                .isEqualTo(DEFAULT_SYMBOL + "0.00");
    }

    @Test
    @DisplayName("formats a large amount correctly")
    void formatsLargeAmount() {
        assertThat(CurrencyFormatter.format(new BigDecimal("12345.678")))
                .isEqualTo(DEFAULT_SYMBOL + "12345.68");
    }

    @Test
    @DisplayName("uses updated symbol after setSymbol")
    void updatedSymbol() {
        CurrencyFormatter.setSymbol("$");
        assertThat(CurrencyFormatter.format(new BigDecimal("10.00")))
                .isEqualTo("$10.00");
    }

    @Test
    @DisplayName("formats a double value")
    void formatsDouble() {
        assertThat(CurrencyFormatter.format(99.5))
                .isEqualTo(DEFAULT_SYMBOL + "99.50");
    }

    @Test
    @DisplayName("formats an exact two-decimal amount unchanged")
    void exactTwoDecimals() {
        assertThat(CurrencyFormatter.format(new BigDecimal("7.77")))
                .isEqualTo(DEFAULT_SYMBOL + "7.77");
    }
}
