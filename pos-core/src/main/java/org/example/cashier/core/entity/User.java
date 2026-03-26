package org.example.cashier.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.cashier.core.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application user — cashiers, managers, admins.
 * Passwords are stored as BCrypt hashes (strength 12) — never plaintext.
 * barcodePin is a unique generated code used for scan-to-login on the cashier screen.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username",     columnList = "username",     unique = true),
        @Index(name = "idx_user_barcode_pin",  columnList = "barcode_pin",  unique = true),
        @Index(name = "idx_user_active",       columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    /** BCrypt hash — never expose in DTOs. */
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Column(name = "email", length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.CASHIER;

    /**
     * Unique barcode PIN — auto-generated on first persist.
     * Encoded as CODE128 on the user's staff badge.
     * Scanning this code in the login screen authenticates the user without a password.
     */
    @Column(name = "barcode_pin", unique = true, length = 20)
    private String barcodePin;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (barcodePin == null || barcodePin.isBlank()) {
            barcodePin = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    @Override
    public String toString() { return username + " [" + role + "]"; }
}
