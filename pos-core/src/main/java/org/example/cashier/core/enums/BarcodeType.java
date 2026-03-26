package org.example.cashier.core.enums;

/**
 * Supported barcode symbologies.
 * CODE128 is default for internal product codes.
 * EAN_13 must be exactly 12 digits (check digit auto-computed by ZXing).
 */
public enum BarcodeType {
    CODE128("Code 128 — alphanumeric, variable length"),
    EAN_13("EAN-13 — 12 digits + computed check digit");

    private final String description;

    BarcodeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
