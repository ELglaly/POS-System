package org.example.cashier.services;

import org.example.cashier.core.entity.User;

import java.util.Optional;

/**
 * Contract for user authentication via password or barcode PIN.
 * Implementations handle lockout logic; callers receive an empty Optional on failure.
 */
public interface AuthService {

    /**
     * Authenticate with username and password.
     * Returns the active {@link User} on success, or empty on bad credentials / locked account.
     */
    Optional<User> login(String username, String password);

    /**
     * Authenticate by scanning a staff barcode PIN.
     * Returns the active {@link User} on success, or empty if the PIN is unknown / account locked.
     */
    Optional<User> loginWithBarcode(String barcodePin);
}
