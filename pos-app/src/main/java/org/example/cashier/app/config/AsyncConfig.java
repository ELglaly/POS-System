package org.example.cashier.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration.
 *
 * "printTaskExecutor" — dedicated pool for label print jobs.
 * Kept small (max 4 threads) because thermal printers queue internally;
 * flooding the printer with threads causes garbled output.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "printTaskExecutor")
    public Executor printTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("print-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}
