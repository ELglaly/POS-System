package org.example.cashier.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.example.cashier.app.config.PosSpringConfig;
import org.example.cashier.app.event.StageReadyEvent;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

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
