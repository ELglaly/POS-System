package org.example.cashier.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;
import org.example.cashier.data.repository.UserRepository;
import org.example.cashier.reports.barcode.ZXingBarcodeEngine;
import org.example.cashier.services.UserManagementService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final ZXingBarcodeEngine barcodeEngine;

    @Override
    public List<User> findAll() {
        return userRepository.findAllByActiveTrueOrderByUsername();
    }

    @Override
    @Transactional
    public User createUser(String username, String fullName, String rawPassword, UserRole role) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username is required.");
        if (rawPassword == null || rawPassword.length() < 4)
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        if (userRepository.existsByUsernameIgnoreCase(username))
            throw new IllegalArgumentException("Username \"" + username + "\" is already taken.");

        User user = User.builder()
                .username(username.trim())
                .fullName(fullName != null ? fullName.trim() : null)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role != null ? role : UserRole.CASHIER)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Created user {} (role: {}, barcodePin: {})",
                saved.getUsername(), saved.getRole(), saved.getBarcodePin());
        return saved;
    }

    @Override
    @Transactional
    public User updateUser(Long id, String username, String fullName,
                           String rawPassword, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username is required.");

        String trimmed = username.trim();
        if (!trimmed.equalsIgnoreCase(user.getUsername())
                && userRepository.existsByUsernameIgnoreCase(trimmed)) {
            throw new IllegalArgumentException("Username \"" + trimmed + "\" is already taken.");
        }

        user.setUsername(trimmed);
        user.setFullName(fullName != null ? fullName.trim() : null);
        user.setRole(role != null ? role : UserRole.CASHIER);

        if (rawPassword != null && !rawPassword.isBlank()) {
            if (rawPassword.length() < 4)
                throw new IllegalArgumentException("Password must be at least 4 characters.");
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }

        User saved = userRepository.save(user);
        log.info("Updated user {} (role: {})", saved.getUsername(), saved.getRole());
        return saved;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
            log.info("Deactivated user {}", u.getUsername());
        });
    }

    @Override
    public byte[] getUserBarcodeLabel(User user) {
        String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                ? user.getFullName() : user.getUsername();
        return barcodeEngine.generateUserBarcodeLabel(user.getBarcodePin(), displayName);
    }
}
