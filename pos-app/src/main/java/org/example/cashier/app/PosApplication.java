package org.example.cashier.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.example.cashier.app.config.PosSpringConfig;
import org.example.cashier.app.event.StageReadyEvent;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JavaFX Application bootstrap that starts the Spring context in init()
 * (background thread) and publishes StageReadyEvent in start() (FX thread).
 *
 * Thread model:
 *   init()  → JavaFX launcher thread → starts Spring (blocking)
 *   start() → JavaFX Application thread → publishes event → listener loads FXML
 *   stop()  → JavaFX Application thread → closes Spring context
 */
public class PosApplication extends Application {

    private static String[] savedArgs = new String[0];

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(PosSpringConfig.class)
                .headless(false)   // required — desktop app has a display
                .run(savedArgs);
    }

    @Override
    public void start(Stage primaryStage) {
        // Publish the stage so StageReadyListener can load the FXML
        springContext.publishEvent(new StageReadyEvent(this, primaryStage));
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        savedArgs = args;
        Application.launch(PosApplication.class, args);
    }
}
