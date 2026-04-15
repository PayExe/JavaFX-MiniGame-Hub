package dev.skypaolo.model;

import java.util.Random;

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
    
    public enum GameState {
        WAITING,
        PLAYING,
        WON,
        LOST
    }
    
    public PlusOuMoinsGame() {
        this.random = new Random();
        this.state = GameState.WAITING;
        this.attempts = 0;
        this.score = 0;
    }
    
    public void startNewGame(String playerName) {
        this.playerName = playerName;
        this.secretNumber = generateSecretNumber();
        this.attempts = 0;
        this.score = 0;
        this.state = GameState.PLAYING;
        
        System.out.println("[VT-OS] New game started for player: " + playerName);
        System.out.println("[VT-OS] Secret number generated (Vault-Tec classified).");
    }
    
    private int generateSecretNumber() {
        return random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
    }
    
    public GuessResult makeGuess(int guess) {
        if (state == GameState.WON || state == GameState.LOST) {
            return GuessResult.GAME_OVER;
        }
        
        if (guess < MIN_NUMBER || guess > MAX_NUMBER) {
            return GuessResult.INVALID;
        }
        
        attempts++;
        
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
    
    private int calculateScore() {
        return (MAX_ATTEMPTS - attempts) * 10;
    }
    
    public void resetGame() {
        if (playerName != null && !playerName.isEmpty()) {
            startNewGame(playerName);
        } else {
            state = GameState.WAITING;
            attempts = 0;
            score = 0;
        }
    }
    
    public boolean isGameOver() {
        return state == GameState.WON || state == GameState.LOST;
    }
    
    public boolean isWon() {
        return state == GameState.WON;
    }
    
    public boolean isLost() {
        return state == GameState.LOST;
    }
    
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
