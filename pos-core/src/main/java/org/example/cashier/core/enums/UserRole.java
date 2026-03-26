package org.example.cashier.core.enums;

/**
 * Role hierarchy: ADMIN > MANAGER > CASHIER > INVENTORY_CLERK.
 * Spring Security requires the "ROLE_" prefix convention — the prefix is
 * added by SecurityConfig; store the bare name here.
 */
public enum UserRole {
    ADMIN,
    MANAGER,
    CASHIER,
    INVENTORY_CLERK
}