package org.example.cashier.core.constants;


public final class BarcodeConstants {

    private BarcodeConstants() {}
    public static final String INTERNAL_PREFIX = "POS";
    public static final String INTERNAL_FORMAT = INTERNAL_PREFIX + "-%s-%d-%d";
    public static final int CODE128_DEFAULT_WIDTH_PX = 400;
    public static final int CODE128_DEFAULT_HEIGHT_PX = 120;
    public static final int EAN13_DEFAULT_WIDTH_PX  = 300;
    public static final int EAN13_DEFAULT_HEIGHT_PX = 100;
    public static final int QUIET_ZONE_MODULES = 10;
    public static final int EAN13_PAYLOAD_LENGTH = 12;
    public static final int MAX_COPIES_PER_JOB = 999;
}
