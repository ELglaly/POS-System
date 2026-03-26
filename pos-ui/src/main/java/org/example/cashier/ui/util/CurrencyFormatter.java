package org.example.cashier.ui.util;

import java.math.BigDecimal;

public class CurrencyFormatter {

    private static String symbol = " EGY ";

    public static void setSymbol(String s) { symbol = s; }

    public static String format(BigDecimal amount) {
        if (amount == null) return symbol + "0.00";
        return symbol + String.format("%.2f", amount);
    }

    public static String format(double amount) {
        return symbol + String.format("%.2f", amount);
    }
}
