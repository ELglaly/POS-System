package org.example.cashier.services;

import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;

import java.util.List;

public interface UserManagementService {

    List<User> findAll();

    User createUser(String username, String fullName, String rawPassword, UserRole role);
    User updateUser(Long id, String username, String fullName, String rawPassword, UserRole role);

    void deleteUser(Long id);

    byte[] getUserBarcodeLabel(User user);
}
