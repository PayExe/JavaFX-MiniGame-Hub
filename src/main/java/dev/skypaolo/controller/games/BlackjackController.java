package dev.skypaolo.controller.games;

import dev.skypaolo.model.BlackjackGame;
import dev.skypaolo.model.Card;
import dev.skypaolo.model.Hand;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Vault-Tec Controller: Blackjack Game
 * Manages the French-language interface and game logic for Blackjack.
 * 
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Game state management and betting logic
 * - Perception: Card display and value calculation
 * - Charisma: French localization and casino atmosphere
 * - Agility: Smooth card dealing animations
 * 
 * "La banque gagne toujours... sauf quand elle perd."
 */
public class BlackjackController implements Initializable {
    
    // FXML Injected Components - Registration Section
    @FXML private VBox registrationSection;
    @FXML private TextField playerNameField;
    @FXML private Button registerButton;
    
    // FXML Injected Components - Betting Section
    @FXML private VBox bettingSection;
    @FXML private Label balanceLabel;
    @FXML private TextField betField;
    @FXML private Button betButton;
    @FXML private Label minBetLabel;
    
    // FXML Injected Components - Game Section
    @FXML private VBox gameSection;
    @FXML private Label currentBetLabel;
    @FXML private Label playerNameDisplay;
    
    // Dealer Area
    @FXML private VBox dealerArea;
    @FXML private HBox dealerCardsBox;
    @FXML private Label dealerValueLabel;
    @FXML private Label dealerStatusLabel;
    
    // Player Area
    @FXML private VBox playerArea;
    @FXML private HBox playerCardsBox;
    @FXML private Label playerValueLabel;
    @FXML private Label playerStatusLabel;
    
    // Action Buttons
    @FXML private HBox actionButtons;
    @FXML private Button hitButton;
    @FXML private Button standButton;
    @FXML private Button doubleButton;
    
    // Result Section
    @FXML private VBox resultSection;
    @FXML private Label resultLabel;
    @FXML private Label resultDetailsLabel;
    @FXML private Button newRoundButton;
    
    // Game Model
    private BlackjackGame game;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Initializing Blackjack simulation...");
        
        // Initialize game model
        game = new BlackjackGame();
        
        // Setup enter key handling
        playerNameField.setOnAction(event -> handleRegister(event));
        betField.setOnAction(event -> handlePlaceBet(event));
        
        // Show registration section initially
        showSection(registrationSection);
        hideSection(bettingSection);
        hideSection(gameSection);
        hideSection(resultSection);
        
        System.out.println("[VT-OS] Blackjack controller ready. Awaiting player registration.");
    }
    
    /**
     * Handle player registration
     */
    @FXML
    public void handleRegister(ActionEvent event) {
        String playerName = playerNameField.getText().trim();
        
        // Validate player name
        if (playerName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nom requis", 
                     "Veuillez entrer votre nom pour jouer.");
            return;
        }
        
        if (playerName.length() > 50) {
            showAlert(Alert.AlertType.WARNING, "Nom trop long", 
                     "Le nom ne doit pas dépasser 50 caractères.");
            return;
        }
        
        Platform.runLater(() -> {
            // Register player
            game.registerPlayer(playerName);
            
            // Update UI
            playerNameDisplay.setText(playerName);
            updateBalanceDisplay();
            minBetLabel.setText("Mise minimum: " + game.getMinimumBet() + " crédits");
            
            // Switch to betting section
            hideSection(registrationSection);
            showSection(bettingSection);
            
            // Focus bet field
            betField.requestFocus();
            
            System.out.println("[VT-OS] Player registered: " + playerName);
        });
    }
    
    /**
     * Handle bet placement
     */
    @FXML
    public void handlePlaceBet(ActionEvent event) {
        String input = betField.getText().trim();
        
        // Parse bet amount
        int betAmount;
        try {
            betAmount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Mise invalide", 
                     "Veuillez entrer un nombre valide.");
            return;
        }
        
        // Place bet
        if (game.placeBet(betAmount)) {
            Platform.runLater(() -> {
                // Update UI
                currentBetLabel.setText("Mise actuelle: " + game.getCurrentBet() + " crédits");
                updateBalanceDisplay();
                
                // Switch to game section
                hideSection(bettingSection);
                showSection(gameSection);
                
                // Display initial hands
                displayHands();
                
                // Check if game is already over (Blackjack scenarios)
                if (game.getState() == BlackjackGame.GameState.GAME_OVER) {
                    showResult();
                } else {
                    // Enable action buttons
                    updateActionButtons();
                }
                
                System.out.println("[VT-OS] Bet placed: " + betAmount + " | Game started.");
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Mise refusée", 
                     game.getResultMessage());
        }
    }
    
    /**
     * Handle Hit action (take a card)
     */
    @FXML
    public void handleHit(ActionEvent event) {
        if (game.hit()) {
            Platform.runLater(() -> {
                // Animate new card
                displayHands();
                
                // Check if game ended
                if (game.getState() == BlackjackGame.GameState.GAME_OVER) {
                    showResult();
                } else {
                    // Update button states (can no longer double after hitting)
                    updateActionButtons();
                }
                
                System.out.println("[VT-OS] Player hits. Hand value: " + 
                                  game.getPlayerHand().getValue());
            });
        }
    }
    
    /**
     * Handle Stand action (end turn)
     */
    @FXML
    public void handleStand(ActionEvent event) {
        if (game.stand()) {
            Platform.runLater(() -> {
                // Disable buttons during dealer turn
                setActionsEnabled(false);
                
                // Display final hands
                displayHands();
                
                // Show result
                showResult();
                
                System.out.println("[VT-OS] Player stands. Dealer plays and reveals cards.");
            });
        }
    }
    
    /**
     * Handle Double Down action
     */
    @FXML
    public void handleDouble(ActionEvent event) {
        if (game.doubleDown()) {
            Platform.runLater(() -> {
                // Update displays
                currentBetLabel.setText("Mise actuelle: " + game.getCurrentBet() + " crédits");
                updateBalanceDisplay();
                displayHands();
                
                // Show result
                showResult();
                
                System.out.println("[VT-OS] Player doubles down.");
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Action impossible", 
                     game.getResultMessage());
        }
    }
    
    /**
     * Handle New Round action
     */
    @FXML
    public void handleNewRound(ActionEvent event) {
        Platform.runLater(() -> {
            // Start new round
            game.newRound();
            
            // Clear displays
            clearHandsDisplay();
            
            // Update balance
            updateBalanceDisplay();
            
            // Hide result and game sections, show betting
            hideSection(resultSection);
            hideSection(gameSection);
            showSection(bettingSection);
            
            // Clear and focus bet field
            betField.clear();
            betField.requestFocus();
            
            // Reset status labels
            playerStatusLabel.setText("");
            dealerStatusLabel.setText("");
            resultLabel.setText("");
            resultDetailsLabel.setText("");
            
            System.out.println("[VT-OS] New round started.");
        });
    }
    
    /**
     * Handle back to hub button
     */
    @FXML
    public void handleBackToHub(ActionEvent event) {
        System.out.println("[VT-OS] Returning to Mini Game Hub...");
        
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MinigameHubView.fxml")
            );
            Parent hubView = loader.load();
            
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(hubView, 800, 600);
            scene.getStylesheets().add(
                getClass().getResource("/css/vault-tec-theme.css").toExternalForm()
            );
            
            currentStage.setScene(scene);
            currentStage.setTitle("Vault-Tec Mini Game Hub");
            
            System.out.println("[VT-OS] Successfully returned to hub.");
            
        } catch (IOException e) {
            System.err.println("[VT-OS CRITICAL] Failed to return to hub: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", 
                     "Impossible de retourner au hub principal.");
        }
    }
    
    /**
     * Display both player and dealer hands
     */
    private void displayHands() {
        displayHand(game.getPlayerHand(), playerCardsBox, playerValueLabel, true);
        
        // For dealer, hide second card if game is still in progress
        boolean hideSecondCard = game.getState() == BlackjackGame.GameState.PLAYER_TURN;
        displayDealerHand(hideSecondCard);
    }
    
    /**
     * Display a hand of cards
     */
    private void displayHand(Hand hand, HBox cardsContainer, Label valueLabel, boolean showAll) {
        cardsContainer.getChildren().clear();
        
        for (int i = 0; i < hand.getCardCount(); i++) {
            Card card = hand.getCard(i);
            Label cardLabel = createCardLabel(card);
            cardsContainer.getChildren().add(cardLabel);
            
            // Add animation for new cards
            if (i == hand.getCardCount() - 1 && hand.getCardCount() > 0) {
                animateCardDeal(cardLabel);
            }
        }
        
        valueLabel.setText("Valeur: " + hand.getValueDisplay());
    }
    
    /**
     * Display dealer hand (with option to hide second card)
     */
    private void displayDealerHand(boolean hideSecondCard) {
        dealerCardsBox.getChildren().clear();
        Hand dealerHand = game.getDealerHand();
        
        for (int i = 0; i < dealerHand.getCardCount(); i++) {
            Label cardLabel;
            
            if (i == 1 && hideSecondCard) {
                // Show hidden card (face down)
                cardLabel = createHiddenCardLabel();
            } else {
                Card card = dealerHand.getCard(i);
                cardLabel = createCardLabel(card);
            }
            
            dealerCardsBox.getChildren().add(cardLabel);
        }
        
        if (hideSecondCard) {
            // Only show value of first card
            int visibleValue = dealerHand.getFirstCard().getValue();
            dealerValueLabel.setText("Valeur visible: " + visibleValue + " (+?)");
        } else {
            dealerValueLabel.setText("Valeur: " + dealerHand.getValueDisplay());
        }
    }
    
    /**
     * Create a label representing a card
     */
    private Label createCardLabel(Card card) {
        Label label = new Label(card.getDisplayString());
        label.getStyleClass().add("card-label");
        
        // Color coding
        if (card.isRed()) {
            label.getStyleClass().add("card-red");
        } else {
            label.getStyleClass().add("card-black");
        }
        
        return label;
    }
    
    /**
     * Create a label for hidden card (face down)
     */
    private Label createHiddenCardLabel() {
        Label label = new Label("🂠");
        label.getStyleClass().addAll("card-label", "card-hidden");
        return label;
    }
    
    /**
     * Animate card dealing with TranslateTransition and FadeTransition
     */
    private void animateCardDeal(Label cardLabel) {
        // Set initial state
        cardLabel.setOpacity(0);
        cardLabel.setTranslateY(-50);
        
        // Translate animation (card drop)
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), cardLabel);
        translateTransition.setFromY(-50);
        translateTransition.setToY(0);
        
        // Fade animation (card appear)
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), cardLabel);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        
        // Play both animations
        translateTransition.play();
        fadeTransition.play();
    }
    
    /**
     * Show game result
     */
    private void showResult() {
        Platform.runLater(() -> {
            // Reveal dealer hand
            displayDealerHand(false);
            
            // Show result section
            showSection(resultSection);
            
            // Update result labels
            BlackjackGame.GameResult result = game.getResult();
            String resultMessage = game.getResultMessage();
            
            resultLabel.setText(resultMessage);
            
            // Style based on result
            resultLabel.getStyleClass().removeAll("feedback-win", "feedback-loss", "feedback-push");
            
            switch (result) {
                case WIN:
                case BLACKJACK_WIN:
                    resultLabel.getStyleClass().add("feedback-win");
                    playerStatusLabel.setText("Gagné!");
                    break;
                case LOSS:
                case DEALER_BLACKJACK:
                    resultLabel.getStyleClass().add("feedback-loss");
                    playerStatusLabel.setText("Perdu...");
                    break;
                case PUSH:
                    resultLabel.getStyleClass().add("feedback-push");
                    playerStatusLabel.setText("Égalité");
                    break;
            }
            
            // Show dealer status
            Hand dealerHand = game.getDealerHand();
            if (dealerHand.isBlackjack()) {
                dealerStatusLabel.setText("Blackjack!");
            } else if (dealerHand.isBust()) {
                dealerStatusLabel.setText("Bust!");
            } else {
                dealerStatusLabel.setText("");
            }
            
            // Update balance display
            updateBalanceDisplay();
            
            // Disable action buttons
            setActionsEnabled(false);
            
            // Enable new round button
            newRoundButton.setDisable(false);
        });
    }
    
    /**
     * Clear hands display
     */
    private void clearHandsDisplay() {
        playerCardsBox.getChildren().clear();
        dealerCardsBox.getChildren().clear();
        playerValueLabel.setText("");
        dealerValueLabel.setText("");
        playerStatusLabel.setText("");
        dealerStatusLabel.setText("");
    }
    
    /**
     * Update balance display
     */
    private void updateBalanceDisplay() {
        String balanceText = "Solde: " + game.getBalance() + " crédits";
        balanceLabel.setText(balanceText);
    }
    
    /**
     * Update action button states
     */
    private void updateActionButtons() {
        hitButton.setDisable(false);
        standButton.setDisable(false);
        doubleButton.setDisable(!game.canDouble());
    }
    
    /**
     * Enable/disable all action buttons
     */
    private void setActionsEnabled(boolean enabled) {
        hitButton.setDisable(!enabled);
        standButton.setDisable(!enabled);
        doubleButton.setDisable(!enabled);
    }
    
    /**
     * Show a section (VBox)
     */
    private void showSection(VBox section) {
        section.setVisible(true);
        section.setManaged(true);
    }
    
    /**
     * Hide a section (VBox)
     */
    private void hideSection(VBox section) {
        section.setVisible(false);
        section.setManaged(false);
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/css/vault-tec-theme.css").toExternalForm()
        );
        
        alert.showAndWait();
    }
}
