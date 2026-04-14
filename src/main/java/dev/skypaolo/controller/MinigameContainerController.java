package dev.skypaolo.controller;

import dev.skypaolo.model.Minigame;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Vault-Tec Controller: Minigame Container
 * Manages the container view with header and content area for minigames.
 * Provides navigation back to the main hub.
 * S.P.E.C.I.A.L. Stats: Perception (navigation), Endurance (content management)
 */
public class MinigameContainerController implements Initializable {
    
    @FXML
    private Button backButton;
    
    @FXML
    private ImageView minigameIcon;
    
    @FXML
    private Label minigameTitle;
    
    @FXML
    private StackPane contentArea;
    
    @FXML
    private VBox loadingContent;
    
    private Minigame currentMinigame;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Minigame Container initialized.");
    }
    
    /**
     * Set the minigame data for this container
     * Updates the header information and automatically loads the game content
     */
    public void setMinigame(Minigame minigame) {
        this.currentMinigame = minigame;
        
        // Update header info
        minigameTitle.setText(minigame.getTitle());
        
        // Load minigame icon
        loadMinigameIcon(minigame.getImagePath());
        
        System.out.println("[VT-OS] Loaded minigame container for: " + minigame.getTitle());
        
        // Automatically load the game content
        loadGameContent();
    }
    
    /**
     * Load the minigame icon/image
     */
    private void loadMinigameIcon(String imagePath) {
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (!image.isError()) {
                minigameIcon.setImage(image);
            } else {
                // Use placeholder if image fails to load
                Image placeholder = new Image(getClass().getResourceAsStream("/images/placeholders/default-placeholder.png"));
                minigameIcon.setImage(placeholder);
            }
        } catch (Exception e) {
            System.err.println("[Vault-Tec Warning] Could not load minigame icon: " + imagePath);
        }
    }
    
    /**
     * Handle back button click - return to main hub
     */
    @FXML
    public void handleBackToHub(ActionEvent event) {
        System.out.println("[VT-OS] Returning to Mini Game Hub...");
        
        try {
            // Load the hub view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MinigameHubView.fxml"));
            Parent hubView = loader.load();
            
            // Get current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Create new scene
            Scene scene = new Scene(hubView, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/vault-tec-theme.css").toExternalForm());
            
            // Transition back to hub
            currentStage.setScene(scene);
            currentStage.setTitle("Vault-Tec Mini Game Hub");
            
            System.out.println("[VT-OS] Successfully returned to Mini Game Hub.");
            
        } catch (IOException e) {
            System.err.println("[Vault-Tec Critical] Failed to return to hub: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load the game content automatically
     * Called when setMinigame is invoked
     */
    private void loadGameContent() {
        if (currentMinigame == null) {
            System.err.println("[Vault-Tec Error] No minigame selected!");
            return;
        }
        
        System.out.println("[VT-OS] Loading simulation: " + currentMinigame.getTitle());
        
        // Clear the loading content and load the actual game
        contentArea.getChildren().clear();
        
        try {
            // Load the specific minigame based on ID
            switch (currentMinigame.getId()) {
                case "plus-ou-moins":
                    loadPlusOuMoinsGame();
                    break;
                default:
                    // Show placeholder for unimplemented games
                    showPlaceholderContent();
                    break;
            }
        } catch (IOException e) {
            System.err.println("[VT-OS CRITICAL] Failed to load minigame: " + e.getMessage());
            e.printStackTrace();
            showErrorContent();
        }
    }
    
    /**
     * Load the Plus ou Moins game
     */
    private void loadPlusOuMoinsGame() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/games/PlusOuMoinsView.fxml"));
        Parent gameView = loader.load();
        contentArea.getChildren().add(gameView);
        System.out.println("[VT-OS] Plus ou Moins simulation loaded successfully.");
    }
    
    /**
     * Show placeholder content for unimplemented games
     */
    private void showPlaceholderContent() {
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setStyle("-fx-padding: 40;");
        
        Label startedLabel = new Label("SIMULATION STARTED");
        startedLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFE600;");
        
        Label minigameNameLabel = new Label(currentMinigame.getTitle());
        minigameNameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #3C8DBC;");
        
        Label placeholderLabel = new Label("Cette simulation n'est pas encore implémentée.\nVault-Tec travaille dur pour vous offrir la meilleure expérience!");
        placeholderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #AAAAAA;");
        placeholderLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        placeholderLabel.setWrapText(true);
        
        Button stopButton = new Button("ARRÊTER LA SIMULATION");
        stopButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        stopButton.setOnAction(e -> resetContentArea());
        
        placeholder.getChildren().addAll(startedLabel, minigameNameLabel, placeholderLabel, stopButton);
        contentArea.getChildren().add(placeholder);
    }
    
    /**
     * Show error content when game fails to load
     */
    private void showErrorContent() {
        VBox errorBox = new VBox(20);
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        errorBox.setStyle("-fx-padding: 40;");
        
        Label errorLabel = new Label("ERREUR DE CHARGEMENT");
        errorLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #DC3545;");
        
        Label messageLabel = new Label("Impossible de charger la simulation.\nVeuillez contacter votre superviseur Vault-Tec.");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #AAAAAA;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageLabel.setWrapText(true);
        
        Button backButton = new Button("RETOUR");
        backButton.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        backButton.setOnAction(e -> resetContentArea());
        
        errorBox.getChildren().addAll(errorLabel, messageLabel, backButton);
        contentArea.getChildren().add(errorBox);
    }
    
    /**
     * Reset the content area and reload the game
     * Used when returning from an error state
     */
    private void resetContentArea() {
        contentArea.getChildren().clear();
        loadGameContent();
    }
    
    /**
     * Get the content area for external minigame integration
     * This allows external controllers to inject their game views
     */
    public StackPane getContentArea() {
        return contentArea;
    }
    
    /**
     * Get the current minigame
     */
    public Minigame getCurrentMinigame() {
        return currentMinigame;
    }
}
