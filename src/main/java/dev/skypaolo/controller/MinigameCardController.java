package dev.skypaolo.controller;

import dev.skypaolo.model.Minigame;
import dev.skypaolo.service.MinigameService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Vault-Tec Controller: Minigame Card Component
 * Manages the logic for individual minigame cards.
 * S.P.E.C.I.A.L. Stats: Perception (event handling), Intelligence (navigation logic)
 */
public class MinigameCardController {
    
    @FXML
    private VBox root;
    
    @FXML
    private ImageView cardImage;
    
    @FXML
    private Label cardTitle;
    
    @FXML
    private Label cardDescription;
    
    @FXML
    private Button playButton;
    
    private Minigame minigame;
    
    /**
     * Initialize the card with minigame data
     */
    public void setMinigame(Minigame minigame) {
        this.minigame = minigame;
        
        // Bind data to UI
        cardTitle.setText(minigame.getTitle());
        cardDescription.setText(minigame.getDescription());
        
        // Load image (with fallback to placeholder)
        loadImage(minigame.getImagePath());
    }
    
    /**
     * Load image from path, fallback to placeholder if not found
     */
    private void loadImage(String imagePath) {
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                // Load placeholder if image fails
                image = new Image(getClass().getResourceAsStream("/images/placeholders/default-placeholder.png"));
            }
            cardImage.setImage(image);
        } catch (Exception e) {
            // If even placeholder fails, use a colored rectangle approach
            System.err.println("[Vault-Tec Warning] Could not load image: " + imagePath);
        }
    }
    
    /**
     * Handle play button click - Navigate to minigame container
     * PRESERVES window size during transition - Vault-Tec UX Standard
     */
    @FXML
    public void handlePlay(ActionEvent event) {
        if (minigame == null) {
            System.err.println("[Vault-Tec Error] No minigame data available!");
            return;
        }
        
        try {
            // Load the container view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MinigameContainerView.fxml"));
            Parent containerView = loader.load();
            
            // Get controller and set the minigame
            MinigameContainerController controller = loader.getController();
            controller.setMinigame(minigame);
            
            // Get current stage and preserve dimensions
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double currentWidth = currentStage.getWidth();
            double currentHeight = currentStage.getHeight();
            
            // Create new scene WITHOUT hardcoded dimensions
            Scene scene = new Scene(containerView);
            scene.getStylesheets().add(getClass().getResource("/css/vault-tec-theme.css").toExternalForm());
            
            // Transition to new view while preserving window size
            currentStage.setScene(scene);
            currentStage.setTitle("Vault-Tec Mini Game Hub - " + minigame.getTitle());
            
            // Restore preserved dimensions
            currentStage.setWidth(currentWidth);
            currentStage.setHeight(currentHeight);
            
        } catch (IOException e) {
            System.err.println("[Vault-Tec Critical] Failed to load minigame container: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the root node for this component
     */
    public VBox getRoot() {
        return root;
    }
}
