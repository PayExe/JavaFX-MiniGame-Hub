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

/**
 * Vault-Tec Controller: Plus ou Moins Game
 * Manages the French-language interface and game logic for the number guessing game.
 * 
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Game state management and validation
 * - Perception: User input handling and feedback
 * - Charisma: French localization and user experience
 * - Endurance: Session persistence and database operations
 */
public class PlusOuMoinsController implements Initializable {
    
    // FXML Injected Components - Name Input Section
    @FXML private VBox nameInputSection;
    @FXML private TextField playerNameField;
    @FXML private Button startButton;
    
    // FXML Injected Components - Game Section
    @FXML private VBox gameSection;
    @FXML private Label playerNameLabel;
    @FXML private Label attemptsLabel;
    @FXML private TextField guessField;
    @FXML private Button validateButton;
    @FXML private Label feedbackLabel;
    @FXML private Label scoreLabel;
    
    // FXML Injected Components - Game Over Section
    @FXML private VBox gameOverSection;
    @FXML private Label gameOverLabel;
    @FXML private Label secretNumberLabel;
    @FXML private Button newGameButton;
    
    // Game Model
    private PlusOuMoinsGame game;
    private DatabaseManager databaseManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Initializing Plus ou Moins simulation...");
        
        // Initialize game model
        game = new PlusOuMoinsGame();
        
        // Initialize database connection
        databaseManager = DatabaseManager.getInstance();
        
        // Clear feedback
        feedbackLabel.setText("");
        
        // Setup enter key handling for player name
        playerNameField.setOnAction(event -> handleStartGame(event));
        
        // Setup enter key handling for guess input
        guessField.setOnAction(event -> handleGuess(event));
        
        System.out.println("[VT-OS] Plus ou Moins controller ready. Awaiting player identification.");
    }
    
    /**
     * Handle start game button click
     * Validates player name and initializes the game
     */
    @FXML
    public void handleStartGame(ActionEvent event) {
        String playerName = playerNameField.getText().trim();
        
        // Validate player name
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
            // Start the game
            game.startNewGame(finalPlayerName);
            
            // Switch to game section with smooth transition
            nameInputSection.setVisible(false);
            nameInputSection.setManaged(false);
            gameSection.setVisible(true);
            gameSection.setManaged(true);
            
            // Force layout update
            gameSection.requestLayout();
            
            // Update UI
            playerNameLabel.setText(finalPlayerName);
            scoreLabel.setText("0 points");
            feedbackLabel.setText("");
            feedbackLabel.getStyleClass().setAll("feedback-label");
            updateAttemptsDisplay();
            
            // Enable controls
            guessField.setDisable(false);
            validateButton.setDisable(false);
            
            // Focus the guess field for immediate input
            guessField.requestFocus();
            
            System.out.println("[VT-OS] Game started for player: " + finalPlayerName);
        });
    }
    
    /**
     * Handle guess validation
     * Processes the player's guess and updates the UI
     */
    @FXML
    public void handleGuess(ActionEvent event) {
        String input = guessField.getText().trim();
        
        // Validate input is a number
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
        
        // Process the guess
        final GuessResult result = game.makeGuess(guess);
        
        // Handle result on JavaFX Application Thread
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
    
    /**
     * Handle game over state
     * Shows appropriate message and saves to database
     * All UI updates run on JavaFX Application Thread for responsiveness
     */
    private void handleGameOver(boolean won) {
        Platform.runLater(() -> {
            // Update score label for both win and loss scenarios
            scoreLabel.setText(game.getScore() + " points");
            
            // Show game over section
            gameOverSection.setVisible(true);
            gameOverSection.setManaged(true);
            
            // Show and enable new game button
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
            
            // Force layout update
            gameOverSection.requestLayout();
            
            // Disable input controls
            validateButton.setDisable(true);
            guessField.setDisable(true);
            
            System.out.println("[VT-OS] Game over UI updated. Won: " + won);
        });
        
        // Save to database (can run in background)
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
    
    /**
     * Handle new game button
     * Resets the game while keeping the player name
     */
    @FXML
    public void handleNewGame(ActionEvent event) {
        Platform.runLater(() -> {
            // Reset game state
            game.resetGame();
            
            // Reset UI
            feedbackLabel.setText("");
            feedbackLabel.getStyleClass().setAll("feedback-label");
            scoreLabel.setText("0 points");
            guessField.clear();
            guessField.setDisable(false);
            validateButton.setDisable(false);
            
            // Hide game over section
            gameOverSection.setVisible(false);
            gameOverSection.setManaged(false);
            newGameButton.setVisible(false);
            newGameButton.setManaged(false);
            
            // Force layout update
            gameSection.requestLayout();
            
            updateAttemptsDisplay();
            
            // Focus the guess field for immediate input
            guessField.requestFocus();
            
            System.out.println("[VT-OS] New game started for player: " + game.getPlayerName());
        });
    }
    
    /**
     * Handle back to hub button
     * Returns to the main minigame hub
     */
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
    
    /**
     * Update attempts display
     */
    private void updateAttemptsDisplay() {
        attemptsLabel.setText(game.getAttemptsRemaining() + "/" + game.getMaxAttempts());
    }
    
    /**
     * Show alert dialog
     * Vault-Tec certified error messaging system
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply Vault-Tec styling if possible
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/vault-tec-theme.css").toExternalForm());
        
        alert.showAndWait();
    }
}
