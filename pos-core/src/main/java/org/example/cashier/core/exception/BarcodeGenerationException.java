package org.example.cashier.core.exception;

/**
 * Thrown when barcode image generation fails (bad format, encoding error, etc.).
 */
public class BarcodeGenerationException extends RuntimeException {

    public BarcodeGenerationException(String message) {
        super(message);
    }

    public BarcodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
