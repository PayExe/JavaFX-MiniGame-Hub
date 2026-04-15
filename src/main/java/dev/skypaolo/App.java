package dev.skypaolo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Vault-Tec Mini Game Hub - Main Application Entry Point
 * VT-OS/OPENCODE v84.2.39 - Serial: VTC-2077-OC-4111
 * 
 * S.P.E.C.I.A.L. Stats:
 * - Strength: Robust architecture
 * - Perception: Clean separation of concerns
 * - Endurance: Singleton service management
 * - Charisma: Beautiful Vault-Tec themed UI
 * - Intelligence: MVC pattern implementation
 * - Agility: Responsive flow layout
 * - Luck: 2 example minigames pre-loaded!
 */
public class App extends Application {

    /**
     * Application dimensions - Vault-Tec standard viewport (HD Edition)
     */
    private static final double APP_WIDTH = 1280;
    private static final double APP_HEIGHT = 720;
    private static final String APP_TITLE = "Vault-Tec Mini Game Hub";
    
    /**
     * Resource paths - Vault-Tec directory structure
     */
    private static final String HUB_VIEW_PATH = "/fxml/MinigameHubView.fxml";
    private static final String CSS_THEME_PATH = "/css/vault-tec-theme.css";

    @Override
    public void start(Stage stage) {
        System.out.println("============================================");
        System.out.println("VAULT-TEC INDUSTRIES TERMINAL SYSTEM");
        System.out.println("ROBCO INDUSTRIES (TM) TERMLINK PROTOCOL");
        System.out.println("UNIFIED OPERATING SYSTEM [UOS] v.84.2.39");
        System.out.println("============================================");
        System.out.println();
        System.out.println("[VT-OS] Initializing Mini Game Hub...");
        System.out.println("[VT-OS] Loading Vault-Tec branded interface...");
        
        try {
            // Load the Mini Game Hub view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(HUB_VIEW_PATH));
            Parent root = loader.load();
            
            // Create scene with Vault-Tec dimensions
            Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
            
            // Apply Vault-Tec CSS theme
            scene.getStylesheets().add(getClass().getResource(CSS_THEME_PATH).toExternalForm());
            
            // Configure stage - Vault-Tec HD standard with minimum safe dimensions
            stage.setTitle(APP_TITLE);
            stage.setScene(scene);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Show the application
            stage.show();
            
            System.out.println("[VT-OS] Mini Game Hub launched successfully!");
            System.out.println("[VT-OS] Vault seal integrity: NOMINAL");
            System.out.println("[VT-OS] Ready for user input...");
            System.out.println();
            System.out.println("Vault-Tec -- Preparing for the Future!");
            
        } catch (Exception e) {
            System.err.println("[VT-OS CRITICAL] Failed to initialize Mini Game Hub!");
            System.err.println("[VT-OS ERROR] " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to error display
            javafx.scene.control.Label errorLabel = new javafx.scene.control.Label(
                "[CRITICAL ERROR] Vault-Tec systems failed to initialize.\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Please contact your Vault-Tec representative immediately."
            );
            javafx.scene.layout.StackPane errorRoot = new javafx.scene.layout.StackPane(errorLabel);
            Scene errorScene = new Scene(errorRoot, 400, 200);
            stage.setScene(errorScene);
            stage.setTitle("VT-OS CRITICAL ERROR");
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
