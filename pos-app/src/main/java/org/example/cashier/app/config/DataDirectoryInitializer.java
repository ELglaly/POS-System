package org.example.cashier.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Configuration
@Slf4j
public class DataDirectoryInitializer {
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
