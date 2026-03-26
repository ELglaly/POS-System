package org.example.cashier.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.entity.User;
import org.example.cashier.data.repository.UserRepository;
import org.example.cashier.services.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Handles username/password and barcode PIN authentication.
 * Locks accounts after 5 consecutive failures for 15 minutes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILURES = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Optional<User> login(String username, String password) {
        Optional<User> opt = userRepository.findByUsernameAndActiveTrue(username);
        if (opt.isEmpty()) {
            log.warn("Login attempt for unknown username: {}", username);
            return Optional.empty();
        }
        User user = opt.get();
        return validateAndRecord(user, passwordEncoder.matches(password, user.getPasswordHash()));
    }

    @Override
    @Transactional
    public Optional<User> loginWithBarcode(String barcodePin) {
        Optional<User> opt = userRepository.findByBarcodePinAndActiveTrue(barcodePin);
        if (opt.isEmpty()) {
            log.warn("Barcode login attempt with unknown PIN");
            return Optional.empty();
        }
        return validateAndRecord(opt.get(), true);
    }

    // ── Private ───────────────────────────────────────────────────────────

    private Optional<User> validateAndRecord(User user, boolean credentialValid) {
        if (user.isLocked()) {
            log.warn("Login denied — account locked until {}", user.getLockedUntil());
            return Optional.empty();
        }

        if (!credentialValid) {
            int failures = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failures);
            if (failures >= MAX_FAILURES) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                log.warn("Account {} locked after {} failures", user.getUsername(), failures);
            }
            userRepository.save(user);
            return Optional.empty();
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User {} logged in (role: {})", user.getUsername(), user.getRole());
        return Optional.of(user);
    }
}
