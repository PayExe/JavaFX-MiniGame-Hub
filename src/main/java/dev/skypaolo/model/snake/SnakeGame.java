package dev.skypaolo.model.snake;

import java.util.ArrayList;
import java.util.List;

public class SnakeGame {
    public static final int GRID_WIDTH = 20;
    public static final int GRID_HEIGHT = 15;
    public static final int CELL_SIZE = 20;

    public static final int CANVAS_WIDTH = GRID_WIDTH * CELL_SIZE;
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * CELL_SIZE;

    private static final long INITIAL_TICK_NS = 200_000_000L;
    private static final long MIN_TICK_NS = 50_000_000L;
    private static final long SPEED_INCREMENT_NS = 5_000_000L;

    private Snake snake;
    private Food food;

    private int score;
    private boolean gameRunning;
    private boolean gameOver;
    private boolean paused;
    private boolean gameStarted;
    private String playerName;
    private long startTime;
    private long endTime;

    private long tickIntervalNs;

    public SnakeGame() {
        reset();
    }

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

    public void startNewGame(String playerName) {
        reset();
        this.playerName = playerName;

        int startX = GRID_WIDTH / 2;
        int startY = GRID_HEIGHT / 2;
        this.snake = new Snake(startX, startY, 3, SnakeDirection.RIGHT);

        this.food = new Food(GRID_WIDTH, GRID_HEIGHT, new ArrayList<>(this.snake.getBody()));

        this.gameRunning = true;
        this.gameStarted = false;

        System.out.println("[VT-OS] Snake game ready for: " + playerName);
    }

    public void startGameplay() {
        if (gameRunning && !gameStarted && !gameOver) {
            this.gameStarted = true;
            this.startTime = System.currentTimeMillis();
            System.out.println("[VT-OS] Snake gameplay started!");
        }
    }

    public void update() {
        if (!gameRunning || !gameStarted || paused || gameOver) {
            return;
        }

        snake.move();

        if (food.isAt(snake.getHead())) {
            consumeFood();
        }

        checkCollisions();
    }

    public void handleInput(SnakeDirection direction) {
        if (!gameRunning || gameOver) {
            return;
        }
        snake.queueDirectionChange(direction);
    }

    private void consumeFood() {
        snake.grow();
        score += food.getValue();
        increaseSpeed();
        respawnFood();
    }

    private void increaseSpeed() {
        tickIntervalNs = Math.max(MIN_TICK_NS, tickIntervalNs - SPEED_INCREMENT_NS);
    }

    private void respawnFood() {
        List<SnakePoint> bodyList = new ArrayList<>(snake.getBody());
        food.respawn(GRID_WIDTH, GRID_HEIGHT, bodyList);
    }

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

    private void triggerGameOver(String reason) {
        this.gameOver = true;
        this.gameRunning = false;
        this.endTime = System.currentTimeMillis();
        System.out.println("[VT-OS] Snake game over: " + reason + " | Score: " + score);
    }

    public void togglePause() {
        if (gameRunning && !gameOver) {
            paused = !paused;
        }
    }

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

    public long getGameDurationMs() {
        if (startTime == 0) return 0;
        long end = gameOver ? endTime : System.currentTimeMillis();
        return end - startTime;
    }

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
