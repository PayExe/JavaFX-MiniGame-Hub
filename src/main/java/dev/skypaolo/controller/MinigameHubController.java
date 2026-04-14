package dev.skypaolo.controller;

import dev.skypaolo.model.Minigame;
import dev.skypaolo.service.MinigameService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Vault-Tec Controller: Minigame Hub Main View
 * Manages the main hub displaying all minigame cards.
 * S.P.E.C.I.A.L. Stats: Intelligence (dynamic loading), Perception (user experience)
 */
public class MinigameHubController implements Initializable {
    
    @FXML
    private FlowPane cardsContainer;
    
    private final MinigameService minigameService;
    
    public MinigameHubController() {
        this.minigameService = MinigameService.getInstance();
    }
    
    /**
     * Initialize the hub view - Vault-Tec standard startup procedure
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Initializing Mini Game Hub...");
        System.out.println("[VT-OS] Detected " + minigameService.getMinigameCount() + " available simulations.");
        
        // Load all minigame cards dynamically
        loadMinigameCards();
        
        System.out.println("[VT-OS] Mini Game Hub initialized successfully.");
        System.out.println("[VT-OS] Vault-Tec -- Preparing for the Future!");
    }
    
    /**
     * Dynamically create and load minigame cards
     * Uses FXMLLoader for each card component
     */
    private void loadMinigameCards() {
        // Clear existing cards
        cardsContainer.getChildren().clear();
        
        // Create a card for each minigame
        for (Minigame minigame : minigameService.getAllMinigames()) {
            try {
                // Load the card component
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/MinigameCard.fxml"));
                VBox card = loader.load();
                
                // Get controller and set minigame data
                MinigameCardController cardController = loader.getController();
                cardController.setMinigame(minigame);
                
                // Add card to container
                cardsContainer.getChildren().add(card);
                
                System.out.println("[VT-OS] Loaded card: " + minigame.getTitle());
                
            } catch (IOException e) {
                System.err.println("[Vault-Tec Error] Failed to load card for: " + minigame.getTitle());
                e.printStackTrace();
            }
        }
        
        // If no cards loaded, show placeholder message
        if (cardsContainer.getChildren().isEmpty()) {
            System.out.println("[VT-OS Warning] No minigames available. Vault dwellers will be bored!");
        }
    }
    
    /**
     * Refresh the hub - reload all cards
     * Useful when minigames are added/removed dynamically
     */
    public void refreshHub() {
        System.out.println("[VT-OS] Refreshing Mini Game Hub...");
        loadMinigameCards();
    }
}
