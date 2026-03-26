package org.example.cashier.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Ensures the application data directory (~/.cashier-pos/) exists before
 * Hikari tries to open the SQLite file.  Runs once at Spring context startup
 * before any DataSource bean is initialised.
 */
@Configuration
@Slf4j
public class DataDirectoryInitializer {

    /**
     * The JDBC URL from application.properties is parsed to extract the directory.
     * Fallback: always ensure ~/.cashier-pos/ exists.
     */
    @PostConstruct
    public void ensureDataDirectory() {
        Path dataDir = Path.of(System.getProperty("user.home"), ".cashier-pos");
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
                log.info("Created POS data directory: {}", dataDir);
            } catch (IOException ex) {
                log.error("Cannot create POS data directory {}: {}", dataDir, ex.getMessage());
                throw new RuntimeException("POS data directory unavailable", ex);
            }
        }
    }
}
