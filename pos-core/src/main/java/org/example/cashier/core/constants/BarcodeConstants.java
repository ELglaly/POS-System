package org.example.cashier.core.constants;

/**
 * Centralised constants for barcode generation and validation.
 */
public final class BarcodeConstants {

    private BarcodeConstants() {}

    /** Prefix for all POS-internal barcode values. */
    public static final String INTERNAL_PREFIX = "POS";

    /**
     * Internal barcode value format (CODE128).
     * Args: categoryCode (String), productId (long), checkDigit (int).
     */
    public static final String INTERNAL_FORMAT = INTERNAL_PREFIX + "-%s-%d-%d";

    /** Default pixel width for CODE128 barcode images. */
    public static final int CODE128_DEFAULT_WIDTH_PX = 400;

    /** Default pixel height for CODE128 barcode images. */
    public static final int CODE128_DEFAULT_HEIGHT_PX = 120;

    /** Default pixel width for EAN-13 barcode images. */
    public static final int EAN13_DEFAULT_WIDTH_PX  = 300;

    /** Default pixel height for EAN-13 barcode images. */
    public static final int EAN13_DEFAULT_HEIGHT_PX = 100;

    /** Quiet zone (modules) around barcode — minimum required by spec is 10. */
    public static final int QUIET_ZONE_MODULES = 10;

    /** Debounce window in milliseconds between scan events. */
    public static final long SCAN_DEBOUNCE_MS = 300L;

    /** EAN-13 requires exactly 12 numeric digits (ZXing auto-computes the 13th). */
    public static final int EAN13_PAYLOAD_LENGTH = 12;

    /** Maximum label copies per single print job. */
    public static final int MAX_COPIES_PER_JOB = 999;
}
