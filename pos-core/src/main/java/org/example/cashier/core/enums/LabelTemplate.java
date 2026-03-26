package org.example.cashier.core.enums;

/**
 * Predefined label layout templates for barcode printing.
 */
public enum LabelTemplate {

    /** Standard retail label: barcode + product name + price. 58mm wide. */
    STANDARD("Standard (58mm)", 58, 30),

    /** Compact shelf label: barcode only + product code. 40mm wide. */
    COMPACT("Compact (40mm)", 40, 20),

    /** Full info label: barcode + QR + name + SKU + category + price. 80mm wide. */
    FULL("Full Info (80mm)", 80, 50),

    /** Large shelf price tag: price prominent, barcode secondary. 80mm wide. */
    SHELF_TAG("Shelf Price Tag (80mm)", 80, 40);

    private final String displayName;
    /** Label width in millimetres. */
    private final int widthMm;
    /** Label height in millimetres. */
    private final int heightMm;

    LabelTemplate(String displayName, int widthMm, int heightMm) {
        this.displayName = displayName;
        this.widthMm = widthMm;
        this.heightMm = heightMm;
    }

    public String getDisplayName() { return displayName; }
    public int getWidthMm()        { return widthMm; }
    public int getHeightMm()       { return heightMm; }

    @Override
    public String toString() { return displayName; }
}