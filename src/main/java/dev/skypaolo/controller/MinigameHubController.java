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

public class MinigameHubController implements Initializable {
    
    @FXML
    private FlowPane cardsContainer;
    
    private final MinigameService minigameService;
    
    public MinigameHubController() {
        this.minigameService = MinigameService.getInstance();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Initializing Mini Game Hub...");
        System.out.println("[VT-OS] Detected " + minigameService.getMinigameCount() + " available simulations.");
        loadMinigameCards();
        System.out.println("[VT-OS] Mini Game Hub initialized successfully.");
        System.out.println("[VT-OS] Vault-Tec -- Preparing for the Future!");
    }
    
    private void loadMinigameCards() {
        cardsContainer.getChildren().clear();
        
        for (Minigame minigame : minigameService.getAllMinigames()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/MinigameCard.fxml"));
                VBox card = loader.load();
                
                MinigameCardController cardController = loader.getController();
                cardController.setMinigame(minigame);
                
                cardsContainer.getChildren().add(card);
                
                System.out.println("[VT-OS] Loaded card: " + minigame.getTitle());
                
            } catch (IOException e) {
                System.err.println("[Vault-Tec Error] Failed to load card for: " + minigame.getTitle());
                e.printStackTrace();
            }
        }
        
        if (cardsContainer.getChildren().isEmpty()) {
            System.out.println("[VT-OS Warning] No minigames available. Vault dwellers will be bored!");
        }
    }
    
    public void refreshHub() {
        System.out.println("[VT-OS] Refreshing Mini Game Hub...");
        loadMinigameCards();
    }
}
