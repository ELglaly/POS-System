package org.example.cashier.app.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Root Spring Boot configuration class.
 *
 * Security auto-configuration is excluded because this is a desktop app with no
 * HTTP servlet layer.  Spring Security is still available on the classpath for
 * BCrypt hashing and programmatic authentication — just not HTTP filter chains.
 *
 * Entity and repository base packages are declared explicitly to work correctly
 * across multi-module classpath JARs (Spring Boot's default scanning stops at
 * the @SpringBootApplication class package).
 */
@SpringBootApplication(
        scanBasePackages = "org.example.cashier",
        exclude = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@EntityScan(basePackages = {
        "org.example.cashier.core.entity"   // Product, User, Category, Barcode, PrintJob, Transaction, TransactionItem
})
@EnableJpaRepositories(basePackages = "org.example.cashier.data.repository")
public class PosSpringConfig {

    /**
     * BCrypt strength-12 encoder — used by the user management service.
     * Declared here to be available throughout the context without a web
     * security config.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
