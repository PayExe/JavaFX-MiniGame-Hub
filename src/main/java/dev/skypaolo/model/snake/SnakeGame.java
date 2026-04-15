package dev.skypaolo.model.snake;

import java.util.ArrayList;
import java.util.List;

/**
 * Vault-Tec Model: Snake Game State
 * Manages the complete game state, rules, and scoring.
 *
 * S.P.E.C.I.A.L. Stats:
 * - Strength: Robust game loop timing
 * - Perception: Collision detection mastery
 * - Endurance: State management under pressure
 * - Charisma: Score calculation fairness
 * - Intelligence: Speed progression algorithm
 * - Agility: Responsive input buffering
 * - Luck: Random food spawn success
 *
 * "The snake that cannot shed its skin must perish."
 * - Nietzsche (probably while playing this game)
 */
public class SnakeGame {
    // Grid configuration
    public static final int GRID_WIDTH = 20;
    public static final int GRID_HEIGHT = 15;
    public static final int CELL_SIZE = 20;

    // Canvas dimensions
    public static final int CANVAS_WIDTH = GRID_WIDTH * CELL_SIZE;
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * CELL_SIZE;

    // Speed configuration
    private static final long INITIAL_TICK_NS = 200_000_000L; // 200ms in nanoseconds
    private static final long MIN_TICK_NS = 50_000_000L;      // 50ms minimum
    private static final long SPEED_INCREMENT_NS = 5_000_000L; // 5ms faster per food

    // Game components
    private Snake snake;
    private Food food;

    // Game state
    private int score;
    private boolean gameRunning;
    private boolean gameOver;
    private boolean paused;
    private boolean gameStarted;  // True when player presses SPACE to start
    private String playerName;
    private long startTime;
    private long endTime;

    // Timing
    private long tickIntervalNs;

    /**
     * Initialize a new game with default settings
     */
    public SnakeGame() {
        reset();
    }

    /**
     * Reset game to initial state without starting
     */
    public void reset() {
        this.score = 0;
        this.gameRunning = false;
        this.gameOver = false;
        this.paused = false;
        this.gameStarted = false;
        this.playerName = "";
        this.tickIntervalNs = INITIAL_TICK_NS;
        this.startTime = 0;
        this.endTime = 0;
        this.snake = null;
        this.food = null;
    }

    /**
     * Start a new game with the specified player
     *
     * @param playerName Name of the player
     */
    public void startNewGame(String playerName) {
        reset();
        this.playerName = playerName;

        // Create snake in the middle, moving right, length 3
        int startX = GRID_WIDTH / 2;
        int startY = GRID_HEIGHT / 2;
        this.snake = new Snake(startX, startY, 3, SnakeDirection.RIGHT);

        // Spawn initial food with proper grid dimensions
        this.food = new Food(GRID_WIDTH, GRID_HEIGHT, new ArrayList<>(this.snake.getBody()));

        // Game is ready but not started yet - waiting for player to press SPACE
        this.gameRunning = true;
        this.gameStarted = false;

        System.out.println("[VT-OS] Snake game ready for: " + playerName);
    }

    /**
     * Start the actual gameplay (called when player presses SPACE)
     */
    public void startGameplay() {
        if (gameRunning && !gameStarted && !gameOver) {
            this.gameStarted = true;
            this.startTime = System.currentTimeMillis();
            System.out.println("[VT-OS] Snake gameplay started!");
        }
    }

    /**
     * Update game state for one tick
     * Should be called at regular intervals during gameplay
     */
    public void update() {
        if (!gameRunning || !gameStarted || paused || gameOver) {
            return;
        }

        // Move snake
        snake.move();

        // Check for food consumption
        if (food.isAt(snake.getHead())) {
            consumeFood();
        }

        // Check collisions
        checkCollisions();
    }

    /**
     * Handle player input for direction change
     *
     * @param direction Requested direction
     */
    public void handleInput(SnakeDirection direction) {
        if (!gameRunning || gameOver) {
            return;
        }
        snake.queueDirectionChange(direction);
    }

    /**
     * Consume food, grow snake, increase score, respawn food
     */
    private void consumeFood() {
        snake.grow();
        score += food.getValue();
        increaseSpeed();
        respawnFood();
    }

    /**
     * Increase game speed by reducing tick interval
     */
    private void increaseSpeed() {
        tickIntervalNs = Math.max(MIN_TICK_NS, tickIntervalNs - SPEED_INCREMENT_NS);
    }

    /**
     * Respawn food at a new valid location
     */
    private void respawnFood() {
        List<SnakePoint> bodyList = new ArrayList<>(snake.getBody());
        food.respawn(GRID_WIDTH, GRID_HEIGHT, bodyList);
    }

    /**
     * Check for collision with walls or self
     */
    private void checkCollisions() {
        if (snake.collidesWithWall(GRID_WIDTH, GRID_HEIGHT)) {
            triggerGameOver("Mur percuté!");
            return;
        }

        if (snake.collidesWithSelf()) {
            triggerGameOver("Auto-collision!");
            return;
        }
    }

    /**
     * Trigger game over state
     *
     * @param reason Reason for game over
     */
    private void triggerGameOver(String reason) {
        this.gameOver = true;
        this.gameRunning = false;
        this.endTime = System.currentTimeMillis();
        System.out.println("[VT-OS] Snake game over: " + reason + " | Score: " + score);
    }

    /**
     * Toggle pause state
     */
    public void togglePause() {
        if (gameRunning && !gameOver) {
            paused = !paused;
        }
    }

    // ==================== GETTERS ====================

    public Snake getSnake() {
        return snake;
    }

    public Food getFood() {
        return food;
    }

    public int getScore() {
        return score;
    }

    public int getSnakeLength() {
        return snake != null ? snake.getLength() : 0;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTickIntervalNs() {
        return tickIntervalNs;
    }

    /**
     * Get game duration in milliseconds
     *
     * @return Duration or 0 if game not started
     */
    public long getGameDurationMs() {
        if (startTime == 0) return 0;
        long end = gameOver ? endTime : System.currentTimeMillis();
        return end - startTime;
    }

    /**
     * Format duration as MM:SS
     *
     * @return Formatted time string
     */
    public String getFormattedDuration() {
        long seconds = getGameDurationMs() / 1000;
        long mins = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    @Override
    public String toString() {
        return "SnakeGame{" +
               "score=" + score +
               ", running=" + gameRunning +
               ", gameOver=" + gameOver +
               ", paused=" + paused +
               ", player='" + playerName + '\'' +
               ", snakeLength=" + getSnakeLength() +
               '}';
    }
}
