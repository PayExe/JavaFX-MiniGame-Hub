package dev.skypaolo.controller.games;

import dev.skypaolo.database.DatabaseManager;
import dev.skypaolo.model.snake.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Vault-Tec Controller: Snake Game
 * Manages the serpentine simulation with real-time rendering and input handling.
 *
 * S.P.E.C.I.A.L. Stats:
 * - Strength: 60FPS AnimationTimer performance
 * - Perception: Precise collision detection
 * - Endurance: Robust game loop and error handling
 * - Charisma: Engaging visual feedback
 * - Intelligence: State management and scoring
 * - Agility: Responsive input buffering
 * - Luck: Random food spawn success
 *
 * "Remember: The snake grows, but the vault stays the same size."
 */
public class SnakeController implements Initializable {

    // FXML Injected Components - Header
    @FXML private Label scoreLabel;
    @FXML private Label lengthLabel;
    @FXML private Label timeLabel;

    // FXML Injected Components - Name Input
    @FXML private VBox nameInputSection;
    @FXML private TextField playerNameField;
    @FXML private Button startButton;

    // FXML Injected Components - Game Area
    @FXML private StackPane gameArea;
    @FXML private Canvas gameCanvas;
    @FXML private VBox startOverlay;
    @FXML private VBox pauseOverlay;
    @FXML private VBox gameOverOverlay;
    @FXML private Label finalScoreLabel;
    @FXML private Label finalLengthLabel;
    @FXML private Button newGameButton;
    @FXML private Label controlsHint;
    @FXML private Button backToHubButton;

    // Game Model
    private SnakeGame game;
    private DatabaseManager databaseManager;

    // Rendering
    private GraphicsContext gc;

    // Game Loop
    private AnimationTimer gameLoop;
    private long lastUpdate = 0;

    // Colors (Vault-Tec themed)
    private static final Color COLOR_BACKGROUND = Color.web("#0d0d0d");
    private static final Color COLOR_GRID = Color.web("#1a1a1a");
    private static final Color COLOR_SNAKE_HEAD = Color.web("#ffe600");
    private static final Color COLOR_SNAKE_BODY = Color.web("#3c8dbc");
    private static final Color COLOR_SNAKE_OUTLINE = Color.web("#2a5a7a");
    private static final Color COLOR_FOOD = Color.web("#28a745");
    private static final Color COLOR_FOOD_GLOW = Color.web("#48c76d");

    // Input state - removed, now using game.isGameStarted()

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Initializing Snake simulation...");

        // Initialize game model
        game = new SnakeGame();

        // Initialize database
        databaseManager = DatabaseManager.getInstance();

        // Setup canvas
        gc = gameCanvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        // Setup game loop
        setupGameLoop();

        // Setup input handling
        setupInputHandlers();

        // Initial render (blank grid)
        render();

        System.out.println("[VT-OS] Snake controller ready. Awaiting player identification.");
    }

    /**
     * Setup the game loop using AnimationTimer
     */
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (game.isGameRunning() && !game.isPaused() && !game.isGameOver()) {
                    // Check if it's time for next tick
                    if (now - lastUpdate >= game.getTickIntervalNs()) {
                        update();
                        lastUpdate = now;
                    }
                }
                // Always render for smooth animations
                render();

                // Update time display
                if (game.isGameRunning()) {
                    Platform.runLater(() -> timeLabel.setText(game.getFormattedDuration()));
                }
            }
        };
        gameLoop.start();
    }

    /**
     * Setup keyboard input handlers
     */
    private void setupInputHandlers() {
        // Key press handler for the scene
        Platform.runLater(() -> {
            Scene scene = gameCanvas.getScene();
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            } else {
                // If scene not ready, try again later
                gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
                    }
                });
            }
        });

        // Enter key on name field
        playerNameField.setOnAction(event -> handleStartGame(event));
    }

    /**
     * Handle keyboard input
     */
    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        // Handle start/pause with space
        if (code == KeyCode.SPACE) {
            if (game.isGameRunning() && !game.isGameStarted() && !game.isGameOver()) {
                startGameplay();
            } else if (game.isGameRunning() && game.isGameStarted()) {
                togglePause();
            }
            event.consume();
            return;
        }

        // Handle direction keys
        if (game.isGameRunning() && !game.isPaused()) {
            switch (code) {
                case UP, W -> {
                    game.handleInput(SnakeDirection.UP);
                    event.consume();
                }
                case DOWN, S -> {
                    game.handleInput(SnakeDirection.DOWN);
                    event.consume();
                }
                case LEFT, A -> {
                    game.handleInput(SnakeDirection.LEFT);
                    event.consume();
                }
                case RIGHT, D -> {
                    game.handleInput(SnakeDirection.RIGHT);
                    event.consume();
                }
            }
        }
    }

    /**
     * Handle start game button
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

        // Initialize game
        game.startNewGame(playerName);

        // Switch to game view
        nameInputSection.setVisible(false);
        nameInputSection.setManaged(false);
        gameArea.setVisible(true);
        gameArea.setManaged(true);
        controlsHint.setVisible(true);

        // Show start overlay (game is ready but not started yet)
        startOverlay.setVisible(true);
        pauseOverlay.setVisible(false);
        gameOverOverlay.setVisible(false);

        // Update displays
        updateDisplays();

        // Initial render
        render();

        System.out.println("[VT-OS] Snake ready to start for player: " + playerName);
    }

    /**
     * Start actual gameplay (after space press)
     */
    private void startGameplay() {
        game.startGameplay();
        startOverlay.setVisible(false);
        System.out.println("[VT-OS] Snake gameplay started!");
    }

    /**
     * Toggle pause state
     */
    private void togglePause() {
        game.togglePause();
        pauseOverlay.setVisible(game.isPaused());
        System.out.println("[VT-OS] Game " + (game.isPaused() ? "paused" : "resumed"));
    }

    /**
     * Update game state
     */
    private void update() {
        game.update();

        // Check for game over
        if (game.isGameOver()) {
            handleGameOver();
        }

        // Update displays
        Platform.runLater(this::updateDisplays);
    }

    /**
     * Render the game
     */
    private void render() {
        // Clear background
        gc.setFill(COLOR_BACKGROUND);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Draw grid (subtle)
        drawGrid();

        // Don't draw game objects if not started
        if (game.getSnake() == null) return;

        // Draw food
        drawFood();

        // Draw snake
        drawSnake();
    }

    /**
     * Draw subtle grid lines
     */
    private void drawGrid() {
        gc.setStroke(COLOR_GRID);
        gc.setLineWidth(1);

        int cellSize = SnakeGame.CELL_SIZE;

        // Vertical lines
        for (int x = 0; x <= SnakeGame.GRID_WIDTH; x++) {
            gc.strokeLine(x * cellSize, 0, x * cellSize, SnakeGame.CANVAS_HEIGHT);
        }

        // Horizontal lines
        for (int y = 0; y <= SnakeGame.GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * cellSize, SnakeGame.CANVAS_WIDTH, y * cellSize);
        }
    }

    /**
     * Draw the snake
     */
    private void drawSnake() {
        Snake snake = game.getSnake();
        if (snake == null) return;

        int cellSize = SnakeGame.CELL_SIZE;
        int padding = 1;

        boolean first = true;
        for (SnakePoint point : snake.getBody()) {
            double x = point.getX() * cellSize + padding;
            double y = point.getY() * cellSize + padding;
            double size = cellSize - (padding * 2);

            if (first) {
                // Draw head
                gc.setFill(COLOR_SNAKE_HEAD);
                gc.fillRoundRect(x, y, size, size, 6, 6);

                // Draw eyes based on direction
                drawEyes(point, snake.getCurrentDirection(), x, y, size);

                first = false;
            } else {
                // Draw body segment
                gc.setFill(COLOR_SNAKE_BODY);
                gc.fillRoundRect(x, y, size, size, 4, 4);

                // Outline
                gc.setStroke(COLOR_SNAKE_OUTLINE);
                gc.setLineWidth(1);
                gc.strokeRoundRect(x, y, size, size, 4, 4);
            }
        }
    }

    /**
     * Draw eyes on snake head
     */
    private void drawEyes(SnakePoint head, SnakeDirection dir, double x, double y, double size) {
        gc.setFill(Color.BLACK);
        double eyeSize = size / 5;
        double eyeOffset = size / 4;

        double eye1X, eye1Y, eye2X, eye2Y;

        switch (dir) {
            case UP -> {
                eye1X = x + eyeOffset;
                eye1Y = y + eyeOffset;
                eye2X = x + size - eyeOffset - eyeSize;
                eye2Y = y + eyeOffset;
            }
            case DOWN -> {
                eye1X = x + eyeOffset;
                eye1Y = y + size - eyeOffset - eyeSize;
                eye2X = x + size - eyeOffset - eyeSize;
                eye2Y = y + size - eyeOffset - eyeSize;
            }
            case LEFT -> {
                eye1X = x + eyeOffset;
                eye1Y = y + eyeOffset;
                eye2X = x + eyeOffset;
                eye2Y = y + size - eyeOffset - eyeSize;
            }
            case RIGHT -> {
                eye1X = x + size - eyeOffset - eyeSize;
                eye1Y = y + eyeOffset;
                eye2X = x + size - eyeOffset - eyeSize;
                eye2Y = y + size - eyeOffset - eyeSize;
            }
            default -> {
                return;
            }
        }

        gc.fillOval(eye1X, eye1Y, eyeSize, eyeSize);
        gc.fillOval(eye2X, eye2Y, eyeSize, eyeSize);
    }

    /**
     * Draw food with pulsing effect
     */
    private void drawFood() {
        Food food = game.getFood();
        if (food == null) return;

        SnakePoint pos = food.getPosition();
        int cellSize = SnakeGame.CELL_SIZE;

        double x = pos.getX() * cellSize;
        double y = pos.getY() * cellSize;

        // Pulsing effect using sine wave
        long now = System.nanoTime();
        double pulse = (Math.sin(now / 200_000_000.0) + 1) / 2; // 0 to 1
        double glowSize = 4 * pulse;

        // Glow effect
        gc.setFill(COLOR_FOOD_GLOW);
        gc.fillOval(x - glowSize, y - glowSize,
                    cellSize + glowSize * 2, cellSize + glowSize * 2);

        // Main food circle
        gc.setFill(COLOR_FOOD);
        double padding = 3;
        gc.fillOval(x + padding, y + padding,
                    cellSize - padding * 2, cellSize - padding * 2);
    }

    /**
     * Update score and length displays
     */
    private void updateDisplays() {
        scoreLabel.setText(String.valueOf(game.getScore()));
        lengthLabel.setText(String.valueOf(game.getSnakeLength()));
    }

    /**
     * Handle game over
     */
    private void handleGameOver() {
        Platform.runLater(() -> {
            // Update final displays
            finalScoreLabel.setText("Score: " + game.getScore());
            finalLengthLabel.setText("Longueur: " + game.getSnakeLength());

            // Show game over overlay
            gameOverOverlay.setVisible(true);
            controlsHint.setVisible(false);

            // Save score to database
            saveScoreToDatabase();
        });
    }

    /**
     * Save game score to database
     */
    private void saveScoreToDatabase() {
        new Thread(() -> {
            boolean saved = databaseManager.saveSnakeScore(
                game.getPlayerName(),
                game.getScore(),
                game.getSnakeLength(),
                game.getGameDurationMs()
            );

            if (saved) {
                System.out.println("[VT-OS] Snake score saved successfully.");
            } else {
                System.err.println("[VT-OS ERROR] Failed to save Snake score.");
            }
        }).start();
    }

    /**
     * Handle new game button
     */
    @FXML
    public void handleNewGame(ActionEvent event) {
        // Reset game state
        game.startNewGame(game.getPlayerName());

        // Reset UI
        gameOverOverlay.setVisible(false);
        startOverlay.setVisible(true);
        pauseOverlay.setVisible(false);
        controlsHint.setVisible(true);

        // Update displays
        updateDisplays();

        // Reset last update for timing
        lastUpdate = 0;

        System.out.println("[VT-OS] New Snake game started for: " + game.getPlayerName());
    }

    /**
     * Handle back to hub button
     */
    @FXML
    public void handleBackToHub(ActionEvent event) {
        System.out.println("[VT-OS] Returning to Mini Game Hub...");

        // Stop game loop
        if (gameLoop != null) {
            gameLoop.stop();
        }

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
     * Show alert dialog
     */
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
