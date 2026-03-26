package org.example.cashier.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;
import org.example.cashier.data.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.countByActiveTrue() > 0) return;

        User admin = User.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin"))
                .fullName("System Administrator")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        admin = userRepository.save(admin);

        User cashier = User.builder()
                .username("cashier")
                .passwordHash(passwordEncoder.encode("cashier123"))
                .fullName("Default Cashier")
                .role(UserRole.CASHIER)
                .active(true)
                .build();
        cashier = userRepository.save(cashier);

        log.info("Default users created. Admin barcode PIN: {} | Cashier barcode PIN: {}",
                admin.getBarcodePin(), cashier.getBarcodePin());
        log.info("Login: admin/admin123 or cashier/cashier123");
    }
}
