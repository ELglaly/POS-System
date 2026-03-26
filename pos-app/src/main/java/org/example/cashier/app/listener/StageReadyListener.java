package org.example.cashier.app.listener;

import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.app.event.StageReadyEvent;
import org.example.cashier.ui.StageManager;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StageReadyListener implements ApplicationListener<StageReadyEvent> {

    private final StageManager stageManager;

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        stageManager.setPrimaryStage(stage);

        try {
            stageManager.showLoginScreen();
            log.info("POS Pro started — login screen loaded");
        } catch (Exception ex) {
            log.error("Failed to load login screen", ex);
            throw new RuntimeException("Cannot start UI: " + ex.getMessage(), ex);
        }
    }
}
