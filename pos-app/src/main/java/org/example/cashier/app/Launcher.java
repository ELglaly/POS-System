package org.example.cashier.app;

/**
 * True application entry point declared in the JAR MANIFEST.
 *
 * This shim exists because the JavaFX runtime requires the class passed to
 * Application.launch() to be reachable on the module path, but Spring Boot's
 * fat-JAR classloader and the JavaFX module system conflict when the main class
 * itself extends javafx.application.Application.  By keeping this class plain,
 * the JVM initialises without the JavaFX module restriction, then
 * PosApplication.main() calls Application.launch() normally.
 */
public class Launcher {

    public static void main(String[] args) {
        PosApplication.main(args);
    }
}
