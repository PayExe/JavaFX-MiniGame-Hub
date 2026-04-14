package dev.skypaolo.model;

import java.util.Random;

/**
 * Vault-Tec Model: Plus ou Moins Game Logic
 * Manages the state and rules of the "Jeu du + ou -" simulation.
 * 
 * Game Rules (per specification):
 * - Secret number between 1 and 1000
 * - Maximum 10 attempts
 * - Score = (maxAttempts - attempts) * 10 when won, 0 when lost
 * 
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Random number generation and scoring algorithm
 * - Perception: Input validation and state tracking
 * - Endurance: Maintains game state through multiple rounds
 */
public class PlusOuMoinsGame {
    
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 1000;
    private static final int MAX_ATTEMPTS = 10;
    
    private int secretNumber;
    private int attempts;
    private int score;
    private String playerName;
    private GameState state;
    private final Random random;
    
    /**
     * Game state enumeration
     */
    public enum GameState {
        WAITING,    // Waiting for player name
        PLAYING,    // Game in progress
        WON,        // Player won
        LOST        // Player lost (max attempts reached)
    }
    
    /**
     * Constructor - Initialize game
     */
    public PlusOuMoinsGame() {
        this.random = new Random();
        this.state = GameState.WAITING;
        this.attempts = 0;
        this.score = 0;
    }
    
    /**
     * Start a new game with the given player name
     * 
     * @param playerName The player's name (session-persistent)
     */
    public void startNewGame(String playerName) {
        this.playerName = playerName;
        this.secretNumber = generateSecretNumber();
        this.attempts = 0;
        this.score = 0;
        this.state = GameState.PLAYING;
        
        System.out.println("[VT-OS] New game started for player: " + playerName);
        System.out.println("[VT-OS] Secret number generated (Vault-Tec classified).");
    }
    
    /**
     * Generate a random secret number between MIN_NUMBER and MAX_NUMBER
     * Vault-Tec certified random number generator
     */
    private int generateSecretNumber() {
        return random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
    }
    
    /**
     * Make a guess and get the result
     * 
     * @param guess The number guessed by the player
     * @return GuessResult indicating the outcome
     */
    public GuessResult makeGuess(int guess) {
        // Check if game is already over
        if (state == GameState.WON || state == GameState.LOST) {
            return GuessResult.GAME_OVER;
        }
        
        // Validate input range
        if (guess < MIN_NUMBER || guess > MAX_NUMBER) {
            return GuessResult.INVALID;
        }
        
        // Increment attempts
        attempts++;
        
        // Check the guess
        if (guess == secretNumber) {
            state = GameState.WON;
            score = calculateScore();
            return GuessResult.CORRECT;
        } else if (attempts >= MAX_ATTEMPTS) {
            state = GameState.LOST;
            score = 0;
            return GuessResult.GAME_OVER;
        } else if (guess < secretNumber) {
            return GuessResult.LOWER;
        } else {
            return GuessResult.HIGHER;
        }
    }
    
    /**
     * Calculate score based on remaining attempts
     * Formula: (maxAttempts - attempts) * 10
     * 
     * @return The calculated score
     */
    private int calculateScore() {
        return (MAX_ATTEMPTS - attempts) * 10;
    }
    
    /**
     * Reset the game state but keep the player name
     * Used for "Nouvelle Partie" functionality
     */
    public void resetGame() {
        if (playerName != null && !playerName.isEmpty()) {
            startNewGame(playerName);
        } else {
            state = GameState.WAITING;
            attempts = 0;
            score = 0;
        }
    }
    
    /**
     * Check if the game is over
     */
    public boolean isGameOver() {
        return state == GameState.WON || state == GameState.LOST;
    }
    
    /**
     * Check if the player has won
     */
    public boolean isWon() {
        return state == GameState.WON;
    }
    
    /**
     * Check if the player has lost
     */
    public boolean isLost() {
        return state == GameState.LOST;
    }
    
    // ==================== GETTERS ====================
    
    public int getSecretNumber() {
        return secretNumber;
    }
    
    public int getAttempts() {
        return attempts;
    }
    
    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }
    
    public int getAttemptsRemaining() {
        return MAX_ATTEMPTS - attempts;
    }
    
    public int getScore() {
        return score;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public GameState getState() {
        return state;
    }
    
    public int getMinNumber() {
        return MIN_NUMBER;
    }
    
    public int getMaxNumber() {
        return MAX_NUMBER;
    }
    
    @Override
    public String toString() {
        return "PlusOuMoinsGame{" +
                "playerName='" + playerName + '\'' +
                ", attempts=" + attempts +
                "/" + MAX_ATTEMPTS +
                ", score=" + score +
                ", state=" + state +
                '}';
    }
}
