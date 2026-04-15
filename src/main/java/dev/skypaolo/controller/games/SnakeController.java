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
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SnakeController implements Initializable {

    @FXML private Label scoreLabel;
    @FXML private Label lengthLabel;
    @FXML private Label timeLabel;

    @FXML private VBox nameInputSection;
    @FXML private TextField playerNameField;
    @FXML private Button startButton;

    @FXML private javafx.scene.layout.StackPane gameArea;
    @FXML private Canvas gameCanvas;
    @FXML private VBox startOverlay;
    @FXML private VBox pauseOverlay;
    @FXML private VBox gameOverOverlay;
    @FXML private Label finalScoreLabel;
    @FXML private Label finalLengthLabel;
    @FXML private Button newGameButton;
    @FXML private Label controlsHint;
    @FXML private Button backToHubButton;

    private SnakeGame game;
    private DatabaseManager databaseManager;

    private GraphicsContext gc;

    private AnimationTimer gameLoop;
    private long lastUpdate = 0;

    private static final Color COLOR_BACKGROUND = Color.web("#0d0d0d");
    private static final Color COLOR_GRID = Color.web("#1a1a1a");
    private static final Color COLOR_SNAKE_HEAD = Color.web("#ffe600");
    private static final Color COLOR_SNAKE_BODY = Color.web("#3c8dbc");
    private static final Color COLOR_SNAKE_OUTLINE = Color.web("#2a5a7a");
    private static final Color COLOR_FOOD = Color.web("#28a745");
    private static final Color COLOR_FOOD_GLOW = Color.web("#48c76d");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[VT-OS] Initializing Snake simulation...");

        game = new SnakeGame();
        databaseManager = DatabaseManager.getInstance();

        gc = gameCanvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        setupGameLoop();
        setupInputHandlers();

        render();

        System.out.println("[VT-OS] Snake controller ready. Awaiting player identification.");
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (game.isGameRunning() && !game.isPaused() && !game.isGameOver()) {
                    if (now - lastUpdate >= game.getTickIntervalNs()) {
                        update();
                        lastUpdate = now;
                    }
                }
                render();

                if (game.isGameRunning()) {
                    Platform.runLater(() -> timeLabel.setText(game.getFormattedDuration()));
                }
            }
        };
        gameLoop.start();
    }

    private void setupInputHandlers() {
        Platform.runLater(() -> {
            Scene scene = gameCanvas.getScene();
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            } else {
                gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
                    }
                });
            }
        });

        playerNameField.setOnAction(event -> handleStartGame(event));
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == KeyCode.SPACE) {
            if (game.isGameRunning() && !game.isGameStarted() && !game.isGameOver()) {
                startGameplay();
            } else if (game.isGameRunning() && game.isGameStarted()) {
                togglePause();
            }
            event.consume();
            return;
        }

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

        game.startNewGame(playerName);

        nameInputSection.setVisible(false);
        nameInputSection.setManaged(false);
        gameArea.setVisible(true);
        gameArea.setManaged(true);
        controlsHint.setVisible(true);

        startOverlay.setVisible(true);
        pauseOverlay.setVisible(false);
        gameOverOverlay.setVisible(false);

        updateDisplays();

        render();

        System.out.println("[VT-OS] Snake ready to start for player: " + playerName);
    }

    private void startGameplay() {
        game.startGameplay();
        startOverlay.setVisible(false);
        System.out.println("[VT-OS] Snake gameplay started!");
    }

    private void togglePause() {
        game.togglePause();
        pauseOverlay.setVisible(game.isPaused());
        System.out.println("[VT-OS] Game " + (game.isPaused() ? "paused" : "resumed"));
    }

    private void update() {
        game.update();

        if (game.isGameOver()) {
            handleGameOver();
        }

        Platform.runLater(this::updateDisplays);
    }

    private void render() {
        gc.setFill(COLOR_BACKGROUND);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        drawGrid();

        if (game.getSnake() == null) return;

        drawFood();
        drawSnake();
    }

    private void drawGrid() {
        gc.setStroke(COLOR_GRID);
        gc.setLineWidth(1);

        int cellSize = SnakeGame.CELL_SIZE;

        for (int x = 0; x <= SnakeGame.GRID_WIDTH; x++) {
            gc.strokeLine(x * cellSize, 0, x * cellSize, SnakeGame.CANVAS_HEIGHT);
        }

        for (int y = 0; y <= SnakeGame.GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * cellSize, SnakeGame.CANVAS_WIDTH, y * cellSize);
        }
    }

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
                gc.setFill(COLOR_SNAKE_HEAD);
                gc.fillRoundRect(x, y, size, size, 6, 6);

                drawEyes(point, snake.getCurrentDirection(), x, y, size);

                first = false;
            } else {
                gc.setFill(COLOR_SNAKE_BODY);
                gc.fillRoundRect(x, y, size, size, 4, 4);

                gc.setStroke(COLOR_SNAKE_OUTLINE);
                gc.setLineWidth(1);
                gc.strokeRoundRect(x, y, size, size, 4, 4);
            }
        }
    }

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

    private void drawFood() {
        Food food = game.getFood();
        if (food == null) return;

        SnakePoint pos = food.getPosition();
        int cellSize = SnakeGame.CELL_SIZE;

        double x = pos.getX() * cellSize;
        double y = pos.getY() * cellSize;

        long now = System.nanoTime();
        double pulse = (Math.sin(now / 200_000_000.0) + 1) / 2;
        double glowSize = 4 * pulse;

        gc.setFill(COLOR_FOOD_GLOW);
        gc.fillOval(x - glowSize, y - glowSize,
                    cellSize + glowSize * 2, cellSize + glowSize * 2);

        gc.setFill(COLOR_FOOD);
        double padding = 3;
        gc.fillOval(x + padding, y + padding,
                    cellSize - padding * 2, cellSize - padding * 2);
    }

    private void updateDisplays() {
        scoreLabel.setText(String.valueOf(game.getScore()));
        lengthLabel.setText(String.valueOf(game.getSnakeLength()));
    }

    private void handleGameOver() {
        Platform.runLater(() -> {
            finalScoreLabel.setText("Score: " + game.getScore());
            finalLengthLabel.setText("Longueur: " + game.getSnakeLength());

            gameOverOverlay.setVisible(true);
            controlsHint.setVisible(false);

            saveScoreToDatabase();
        });
    }

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

    @FXML
    public void handleNewGame(ActionEvent event) {
        game.startNewGame(game.getPlayerName());

        gameOverOverlay.setVisible(false);
        startOverlay.setVisible(true);
        pauseOverlay.setVisible(false);
        controlsHint.setVisible(true);

        updateDisplays();

        lastUpdate = 0;

        System.out.println("[VT-OS] New Snake game started for: " + game.getPlayerName());
    }

    @FXML
    public void handleBackToHub(ActionEvent event) {
        System.out.println("[VT-OS] Returning to Mini Game Hub...");

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
