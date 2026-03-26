package org.example.cashier.app.event;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

/**
 * Published by PosApplication.start() once the primary JavaFX Stage is ready.
 * StageReadyListener listens for this event and loads the initial FXML scene.
 */
public class StageReadyEvent extends ApplicationEvent {

    public StageReadyEvent(Object source, Stage stage) {
        super(stage);
    }

    public Stage getStage() {
        return (Stage) getSource();
    }
}
