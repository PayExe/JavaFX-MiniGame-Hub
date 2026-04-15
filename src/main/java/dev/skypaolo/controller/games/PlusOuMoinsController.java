package dev.skypaolo.controller.games;

import dev.skypaolo.database.DatabaseManager;
import dev.skypaolo.model.GuessResult;
import dev.skypaolo.model.PlusOuMoinsGame;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlusOuMoinsController implements Initializable {
    
    @FXML private VBox nameInputSection;
    @FXML private TextField playerNameField;
    @FXML private Button startButton;
    
    @FXML private VBox gameSection;
    @FXML private Label playerNameLabel;
    @FXML private Label attemptsLabel;
    @FXML private TextField guessField;
    @FXML private Button validateButton;
    @FXML private Label feedbackLabel;
    @FXML private Label scoreLabel;
    
    @FXML private VBox gameOverSection;
    @FXML private Label gameOverLabel;
    @FXML private Label secretNumberLabel;
    @FXML private Button newGameButton;
    
    private PlusOuMoinsGame game;
    private DatabaseManager databaseManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Initializing Plus ou Moins simulation...");
        
        game = new PlusOuMoinsGame();
        databaseManager = DatabaseManager.getInstance();
        
        feedbackLabel.setText("");
        
        playerNameField.setOnAction(event -> handleStartGame(event));
        guessField.setOnAction(event -> handleGuess(event));
        
        System.out.println("[VT-OS] Plus ou Moins controller ready. Awaiting player identification.");
    }
    
    @FXML
    public void handleStartGame(ActionEvent event) {
        String playerName = playerNameField.getText().trim();
        
        if (playerName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, 
                     "Nom requis", 
                     "Veuillez entrer votre nom pour commencer.");
            return;
        }
        
        if (playerName.length() > 50) {
            showAlert(Alert.AlertType.WARNING, 
                     "Nom trop long", 
                     "Le nom ne doit pas dépasser 50 caractères.");
            return;
        }
        
        final String finalPlayerName = playerName;
        
        Platform.runLater(() -> {
            game.startNewGame(finalPlayerName);
            
            nameInputSection.setVisible(false);
            nameInputSection.setManaged(false);
            gameSection.setVisible(true);
            gameSection.setManaged(true);
            
            gameSection.requestLayout();
            
            playerNameLabel.setText(finalPlayerName);
            scoreLabel.setText("0 points");
            feedbackLabel.setText("");
            feedbackLabel.getStyleClass().setAll("feedback-label");
            updateAttemptsDisplay();
            
            guessField.setDisable(false);
            validateButton.setDisable(false);
            
            guessField.requestFocus();
            
            System.out.println("[VT-OS] Game started for player: " + finalPlayerName);
        });
    }
    
    @FXML
    public void handleGuess(ActionEvent event) {
        String input = guessField.getText().trim();
        
        final int guess;
        try {
            guess = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, 
                     "Entrée invalide", 
                     "Veuillez entrer un nombre valide entre 1 et 1000.");
            Platform.runLater(() -> guessField.clear());
            return;
        }
        
        final GuessResult result = game.makeGuess(guess);
        
        Platform.runLater(() -> {
            switch (result) {
                case INVALID:
                    showAlert(Alert.AlertType.WARNING, 
                             "Nombre hors limites", 
                             "Le nombre doit être entre 1 et 1000.");
                    guessField.clear();
                    break;
                    
                case LOWER:
                    feedbackLabel.setText("C'est plus grand! (" + guess + ")");
                    feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-lower");
                    guessField.clear();
                    updateAttemptsDisplay();
                    guessField.requestFocus();
                    break;
                    
                case HIGHER:
                    feedbackLabel.setText("C'est plus petit! (" + guess + ")");
                    feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-higher");
                    guessField.clear();
                    updateAttemptsDisplay();
                    guessField.requestFocus();
                    break;
                    
                case CORRECT:
                    feedbackLabel.setText("Bravo! Nombre trouvé!");
                    feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-win");
                    updateAttemptsDisplay();
                    handleGameOver(true);
                    break;
                    
                case GAME_OVER:
                    feedbackLabel.setText("Partie terminée!");
                    updateAttemptsDisplay();
                    handleGameOver(false);
                    break;
            }
        });
    }
    
    private void handleGameOver(boolean won) {
        Platform.runLater(() -> {
            scoreLabel.setText(game.getScore() + " points");
            
            gameOverSection.setVisible(true);
            gameOverSection.setManaged(true);
            
            newGameButton.setVisible(true);
            newGameButton.setManaged(true);
            newGameButton.setDisable(false);
            
            if (won) {
                gameOverLabel.setText("Félicitations, " + game.getPlayerName() + "!");
                gameOverLabel.getStyleClass().setAll("game-over-message", "feedback-win");
                secretNumberLabel.setText("Vous avez trouvé en " + game.getAttempts() + 
                                         " tentatives et gagné " + game.getScore() + " points!");
            } else {
                gameOverLabel.setText("Dommage, " + game.getPlayerName() + "...");
                gameOverLabel.getStyleClass().setAll("game-over-message", "feedback-loss");
                secretNumberLabel.setText("Le nombre secret était: " + game.getSecretNumber());
            }
            
            gameOverSection.requestLayout();
            
            validateButton.setDisable(true);
            guessField.setDisable(true);
            
            System.out.println("[VT-OS] Game over UI updated. Won: " + won);
        });
        
        new Thread(() -> {
            boolean saved = databaseManager.saveGameResult(
                game.getPlayerName(),
                game.getScore(),
                game.getAttempts(),
                game.getSecretNumber()
            );
            
            if (saved) {
                System.out.println("[VT-OS] Game result saved to database.");
            } else {
                System.err.println("[VT-OS ERROR] Failed to save game result.");
            }
        }).start();
    }
    
    @FXML
    public void handleNewGame(ActionEvent event) {
        Platform.runLater(() -> {
            game.resetGame();
            
            feedbackLabel.setText("");
            feedbackLabel.getStyleClass().setAll("feedback-label");
            scoreLabel.setText("0 points");
            guessField.clear();
            guessField.setDisable(false);
            validateButton.setDisable(false);
            
            gameOverSection.setVisible(false);
            gameOverSection.setManaged(false);
            newGameButton.setVisible(false);
            newGameButton.setManaged(false);
            
            gameSection.requestLayout();
            
            updateAttemptsDisplay();
            
            guessField.requestFocus();
            
            System.out.println("[VT-OS] New game started for player: " + game.getPlayerName());
        });
    }
    
    @FXML
    public void handleBackToHub(ActionEvent event) {
        System.out.println("[VT-OS] Returning to Mini Game Hub...");
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MinigameHubView.fxml"));
            Parent hubView = loader.load();
            
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(hubView, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/vault-tec-theme.css").toExternalForm());
            
            currentStage.setScene(scene);
            currentStage.setTitle("Vault-Tec Mini Game Hub");
            
            System.out.println("[VT-OS] Successfully returned to hub.");
            
        } catch (IOException e) {
            System.err.println("[VT-OS CRITICAL] Failed to return to hub: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                     "Erreur de navigation", 
                     "Impossible de retourner au hub principal.");
        }
    }
    
    private void updateAttemptsDisplay() {
        attemptsLabel.setText(game.getAttemptsRemaining() + "/" + game.getMaxAttempts());
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/vault-tec-theme.css").toExternalForm());
        
        alert.showAndWait();
    }
}
