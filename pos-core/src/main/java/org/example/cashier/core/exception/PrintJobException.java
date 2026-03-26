package org.example.cashier.core.exception;

/**
 * Thrown when a print job cannot be submitted or completed.
 */
public class PrintJobException extends RuntimeException {

    public PrintJobException(String message) {
        super(message);
    }

    public PrintJobException(String message, Throwable cause) {
        super(message, cause);
    }
}
