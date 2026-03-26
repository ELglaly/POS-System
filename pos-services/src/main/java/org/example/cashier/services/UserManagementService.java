package org.example.cashier.services;

import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;

import java.util.List;

/**
 * Admin-only CRUD operations for user accounts.
 * All password handling (hashing, validation) is encapsulated here.
 */
public interface UserManagementService {

    /** All currently active users, sorted by username. */
    List<User> findAll();

    /**
     * Create a new active user.
     *
     * @throws IllegalArgumentException if username is blank, taken, or password is too short
     */
    User createUser(String username, String fullName, String rawPassword, UserRole role);

    /**
     * Update an existing user's profile.
     * Pass {@code rawPassword} as null/blank to leave the password unchanged.
     *
     * @throws IllegalArgumentException if the user is not found or the new username is taken
     */
    User updateUser(Long id, String username, String fullName, String rawPassword, UserRole role);

    /**
     * Soft-delete: marks the account inactive. Data is preserved for audit.
     */
    void deleteUser(Long id);

    /**
     * Generate a staff badge PNG: CODE128 barcode of the user's barcodePin + display name.
     */
    byte[] getUserBarcodeLabel(User user);
}
