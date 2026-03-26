package org.example.cashier.services;

import org.example.cashier.core.entity.User;

import java.util.Optional;

public interface AuthService {

    Optional<User> login(String username, String password);
    Optional<User> loginWithBarcode(String barcodePin);
}
