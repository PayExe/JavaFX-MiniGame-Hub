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

public class BlackjackController implements Initializable {
    
    @FXML private VBox registrationSection;
    @FXML private TextField playerNameField;
    @FXML private Button registerButton;
    
    @FXML private VBox bettingSection;
    @FXML private Label balanceLabel;
    @FXML private TextField betField;
    @FXML private Button betButton;
    @FXML private Label minBetLabel;
    
    @FXML private VBox gameSection;
    @FXML private Label currentBetLabel;
    @FXML private Label playerNameDisplay;
    
    @FXML private VBox dealerArea;
    @FXML private HBox dealerCardsBox;
    @FXML private Label dealerValueLabel;
    @FXML private Label dealerStatusLabel;
    
    @FXML private VBox playerArea;
    @FXML private HBox playerCardsBox;
    @FXML private Label playerValueLabel;
    @FXML private Label playerStatusLabel;
    
    @FXML private HBox actionButtons;
    @FXML private Button hitButton;
    @FXML private Button standButton;
    @FXML private Button doubleButton;
    
    @FXML private VBox resultSection;
    @FXML private Label resultLabel;
    @FXML private Label resultDetailsLabel;
    @FXML private Button newRoundButton;
    
    private BlackjackGame game;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Initializing Blackjack simulation...");
        
        game = new BlackjackGame();
        
        playerNameField.setOnAction(event -> handleRegister(event));
        betField.setOnAction(event -> handlePlaceBet(event));
        
        showSection(registrationSection);
        hideSection(bettingSection);
        hideSection(gameSection);
        hideSection(resultSection);
        
        System.out.println("[VT-OS] Blackjack controller ready. Awaiting player registration.");
    }
    
    @FXML
    public void handleRegister(ActionEvent event) {
        String playerName = playerNameField.getText().trim();
        
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
            game.registerPlayer(playerName);
            
            playerNameDisplay.setText(playerName);
            updateBalanceDisplay();
            minBetLabel.setText("Mise minimum: " + game.getMinimumBet() + " crédits");
            
            hideSection(registrationSection);
            showSection(bettingSection);
            
            betField.requestFocus();
            
            System.out.println("[VT-OS] Player registered: " + playerName);
        });
    }
    
    @FXML
    public void handlePlaceBet(ActionEvent event) {
        String input = betField.getText().trim();
        
        int betAmount;
        try {
            betAmount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Mise invalide", 
                     "Veuillez entrer un nombre valide.");
            return;
        }
        
        if (game.placeBet(betAmount)) {
            Platform.runLater(() -> {
                currentBetLabel.setText("Mise actuelle: " + game.getCurrentBet() + " crédits");
                updateBalanceDisplay();
                
                hideSection(bettingSection);
                showSection(gameSection);
                
                displayHands();
                
                if (game.getState() == BlackjackGame.GameState.GAME_OVER) {
                    showResult();
                } else {
                    updateActionButtons();
                }
                
                System.out.println("[VT-OS] Bet placed: " + betAmount + " | Game started.");
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Mise refusée", 
                     game.getResultMessage());
        }
    }
    
    @FXML
    public void handleHit(ActionEvent event) {
        if (game.hit()) {
            Platform.runLater(() -> {
                displayHands();
                
                if (game.getState() == BlackjackGame.GameState.GAME_OVER) {
                    showResult();
                } else {
                    updateActionButtons();
                }
                
                System.out.println("[VT-OS] Player hits. Hand value: " + 
                                  game.getPlayerHand().getValue());
            });
        }
    }
    
    @FXML
    public void handleStand(ActionEvent event) {
        if (game.stand()) {
            Platform.runLater(() -> {
                setActionsEnabled(false);
                displayHands();
                showResult();
                
                System.out.println("[VT-OS] Player stands. Dealer plays and reveals cards.");
            });
        }
    }
    
    @FXML
    public void handleDouble(ActionEvent event) {
        if (game.doubleDown()) {
            Platform.runLater(() -> {
                currentBetLabel.setText("Mise actuelle: " + game.getCurrentBet() + " crédits");
                updateBalanceDisplay();
                displayHands();
                showResult();
                
                System.out.println("[VT-OS] Player doubles down.");
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Action impossible", 
                     game.getResultMessage());
        }
    }
    
    @FXML
    public void handleNewRound(ActionEvent event) {
        Platform.runLater(() -> {
            game.newRound();
            clearHandsDisplay();
            updateBalanceDisplay();
            
            hideSection(resultSection);
            hideSection(gameSection);
            showSection(bettingSection);
            
            betField.clear();
            betField.requestFocus();
            
            playerStatusLabel.setText("");
            dealerStatusLabel.setText("");
            resultLabel.setText("");
            resultDetailsLabel.setText("");
            
            System.out.println("[VT-OS] New round started.");
        });
    }
    
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
    
    private void displayHands() {
        displayHand(game.getPlayerHand(), playerCardsBox, playerValueLabel, true);
        
        boolean hideSecondCard = game.getState() == BlackjackGame.GameState.PLAYER_TURN;
        displayDealerHand(hideSecondCard);
    }
    
    private void displayHand(Hand hand, HBox cardsContainer, Label valueLabel, boolean showAll) {
        cardsContainer.getChildren().clear();
        
        for (int i = 0; i < hand.getCardCount(); i++) {
            Card card = hand.getCard(i);
            Label cardLabel = createCardLabel(card);
            cardsContainer.getChildren().add(cardLabel);
            
            if (i == hand.getCardCount() - 1 && hand.getCardCount() > 0) {
                animateCardDeal(cardLabel);
            }
        }
        
        valueLabel.setText("Valeur: " + hand.getValueDisplay());
    }
    
    private void displayDealerHand(boolean hideSecondCard) {
        dealerCardsBox.getChildren().clear();
        Hand dealerHand = game.getDealerHand();
        
        for (int i = 0; i < dealerHand.getCardCount(); i++) {
            Label cardLabel;
            
            if (i == 1 && hideSecondCard) {
                cardLabel = createHiddenCardLabel();
            } else {
                Card card = dealerHand.getCard(i);
                cardLabel = createCardLabel(card);
            }
            
            dealerCardsBox.getChildren().add(cardLabel);
        }
        
        if (hideSecondCard) {
            int visibleValue = dealerHand.getFirstCard().getValue();
            dealerValueLabel.setText("Valeur visible: " + visibleValue + " (+?)");
        } else {
            dealerValueLabel.setText("Valeur: " + dealerHand.getValueDisplay());
        }
    }
    
    private Label createCardLabel(Card card) {
        Label label = new Label(card.getDisplayString());
        label.getStyleClass().add("card-label");
        
        if (card.isRed()) {
            label.getStyleClass().add("card-red");
        } else {
            label.getStyleClass().add("card-black");
        }
        
        return label;
    }
    
    private Label createHiddenCardLabel() {
        Label label = new Label("🂠");
        label.getStyleClass().addAll("card-label", "card-hidden");
        return label;
    }
    
    private void animateCardDeal(Label cardLabel) {
        cardLabel.setOpacity(0);
        cardLabel.setTranslateY(-50);
        
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), cardLabel);
        translateTransition.setFromY(-50);
        translateTransition.setToY(0);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), cardLabel);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        
        translateTransition.play();
        fadeTransition.play();
    }
    
    private void showResult() {
        Platform.runLater(() -> {
            displayDealerHand(false);
            showSection(resultSection);
            
            BlackjackGame.GameResult result = game.getResult();
            String resultMessage = game.getResultMessage();
            
            resultLabel.setText(resultMessage);
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
            
            Hand dealerHand = game.getDealerHand();
            if (dealerHand.isBlackjack()) {
                dealerStatusLabel.setText("Blackjack!");
            } else if (dealerHand.isBust()) {
                dealerStatusLabel.setText("Bust!");
            } else {
                dealerStatusLabel.setText("");
            }
            
            updateBalanceDisplay();
            setActionsEnabled(false);
            newRoundButton.setDisable(false);
        });
    }
    
    private void clearHandsDisplay() {
        playerCardsBox.getChildren().clear();
        dealerCardsBox.getChildren().clear();
        playerValueLabel.setText("");
        dealerValueLabel.setText("");
        playerStatusLabel.setText("");
        dealerStatusLabel.setText("");
    }
    
    private void updateBalanceDisplay() {
        String balanceText = "Solde: " + game.getBalance() + " crédits";
        balanceLabel.setText(balanceText);
    }
    
    private void updateActionButtons() {
        hitButton.setDisable(false);
        standButton.setDisable(false);
        doubleButton.setDisable(!game.canDouble());
    }
    
    private void setActionsEnabled(boolean enabled) {
        hitButton.setDisable(!enabled);
        standButton.setDisable(!enabled);
        doubleButton.setDisable(!enabled);
    }
    
    private void showSection(VBox section) {
        section.setVisible(true);
        section.setManaged(true);
    }
    
    private void hideSection(VBox section) {
        section.setVisible(false);
        section.setManaged(false);
    }
    
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
