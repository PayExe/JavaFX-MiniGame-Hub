package dev.skypaolo.model;

import dev.skypaolo.database.DatabaseManager;

/**
 * Vault-Tec Model: Blackjack Game
 * Manages the state, rules, and logic of a Blackjack simulation.
 * 
 * Game Rules (per specification):
 * - Standard 52-card deck, shuffled
 * - Player vs Dealer (AI)
 * - Initial deal: 2 cards each, 1 dealer card hidden
 * - Player actions: Hit, Stand, Double
 * - Dealer draws until ≥ 17
 * - Natural Blackjack = ×1.5 payout
 * - Bust (>21) = automatic loss
 * - Tie = refund of bet
 * - Minimum balance to play: 1
 * 
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Game rule implementation and payout calculation
 * - Perception: State management and win/loss detection
 * - Luck: Fair deck randomization
 * 
 * "The cards don't lie, but the dealer might smile." - Vault-Tec Gaming Wisdom
 */
public class BlackjackGame {
    
    // Constants
    private static final int INITIAL_BALANCE = 1000;
    private static final int MINIMUM_BET = 10;
    private static final int DEALER_HIT_THRESHOLD = 17;
    private static final double BLACKJACK_PAYOUT_RATIO = 1.5;
    
    /**
     * Game state enumeration
     */
    public enum GameState {
        WAITING,        // Waiting for player name/registration
        BETTING,        // Waiting for player to place bet
        PLAYER_TURN,    // Player's turn to act
        DEALER_TURN,    // Dealer's turn (after player stands)
        GAME_OVER       // Round complete, show results
    }
    
    /**
     * Game result enumeration
     */
    public enum GameResult {
        WIN,            // Player wins normal hand
        LOSS,           // Player loses
        PUSH,           // Tie - bet refunded
        BLACKJACK_WIN,  // Player wins with natural Blackjack (3:2 payout)
        DEALER_BLACKJACK // Dealer has Blackjack
    }
    
    // Game components
    private Deck deck;
    private Hand playerHand;
    private Hand dealerHand;
    
    // Player info
    private String playerName;
    private int balance;
    private int currentBet;
    
    // Game state
    private GameState state;
    private GameResult result;
    private String resultMessage;
    private int winnings;
    
    // Database
    private DatabaseManager databaseManager;
    private int playerId;
    
    /**
     * Constructor - Initialize a new Blackjack game
     */
    public BlackjackGame() {
        this.databaseManager = DatabaseManager.getInstance();
        this.deck = new Deck();
        this.playerHand = new Hand("Joueur");
        this.dealerHand = new Hand("Croupier");
        this.balance = INITIAL_BALANCE;
        this.currentBet = 0;
        this.state = GameState.WAITING;
        this.result = null;
        this.resultMessage = "";
        this.winnings = 0;
        this.playerId = -1;
        
        System.out.println("[VT-OS] Blackjack simulation initialized. Vault-Tec Casino: OPEN");
    }
    
    /**
     * Register a player and load their balance from database
     * 
     * @param playerName The player's name
     */
    public void registerPlayer(String playerName) {
        this.playerName = playerName;
        
        // Get or create player in database
        this.playerId = databaseManager.getOrCreateBlackjackPlayerId(playerName);
        
        // Load existing balance or use initial
        int savedBalance = databaseManager.getBlackjackPlayerBalance(playerId);
        this.balance = savedBalance > 0 ? savedBalance : INITIAL_BALANCE;
        
        this.state = GameState.BETTING;
        
        System.out.println("[VT-OS] Player registered: " + playerName + " | Balance: " + balance);
    }
    
    /**
     * Place a bet for the current round
     * 
     * @param betAmount The amount to bet
     * @return true if bet is valid and placed, false otherwise
     */
    public boolean placeBet(int betAmount) {
        if (state != GameState.BETTING) {
            resultMessage = "Vous ne pouvez pas miser maintenant.";
            return false;
        }
        
        if (betAmount < MINIMUM_BET) {
            resultMessage = "La mise minimum est de " + MINIMUM_BET + " crédits.";
            return false;
        }
        
        if (betAmount > balance) {
            resultMessage = "Solde insuffisant! Vous avez " + balance + " crédits.";
            return false;
        }
        
        this.currentBet = betAmount;
        this.balance -= betAmount; // Deduct bet from balance
        
        // Deal initial cards
        dealInitialCards();
        
        // Check for immediate Blackjacks
        if (checkForBlackjacks()) {
            state = GameState.GAME_OVER;
        } else {
            state = GameState.PLAYER_TURN;
        }
        
        System.out.println("[VT-OS] Bet placed: " + betAmount + " | Balance: " + balance);
        return true;
    }
    
    /**
     * Deal initial cards (2 to player, 2 to dealer)
     */
    private void dealInitialCards() {
        playerHand.clear();
        dealerHand.clear();
        
        // Check if deck needs reshuffling
        if (deck.needsReshuffle()) {
            deck.reset();
        }
        
        // Deal alternating: Player, Dealer, Player, Dealer
        playerHand.addCard(deck.dealCard());
        dealerHand.addCard(deck.dealCard());
        playerHand.addCard(deck.dealCard());
        dealerHand.addCard(deck.dealCard());
        
        System.out.println("[VT-OS] Cards dealt. Player: " + playerHand.getValue() + 
                          " | Dealer shows: " + dealerHand.getFirstCard().getDisplayString());
    }
    
    /**
     * Check for natural Blackjacks after initial deal
     * 
     * @return true if game ended due to Blackjack
     */
    private boolean checkForBlackjacks() {
        boolean playerHasBlackjack = playerHand.isBlackjack();
        boolean dealerHasBlackjack = dealerHand.isBlackjack();
        
        if (playerHasBlackjack && dealerHasBlackjack) {
            // Both have Blackjack - push
            result = GameResult.PUSH;
            resultMessage = "Double Blackjack! Égalité.";
            balance += currentBet; // Refund
            winnings = 0;
            saveGameResult();
            return true;
        } else if (playerHasBlackjack) {
            // Player has Blackjack - 3:2 payout
            result = GameResult.BLACKJACK_WIN;
            winnings = (int) (currentBet * BLACKJACK_PAYOUT_RATIO);
            balance += currentBet + winnings;
            resultMessage = "Blackjack! Vous gagnez " + winnings + " crédits!";
            saveGameResult();
            return true;
        } else if (dealerHasBlackjack) {
            // Dealer has Blackjack - player loses
            result = GameResult.DEALER_BLACKJACK;
            winnings = -currentBet;
            resultMessage = "Le croupier a un Blackjack! Vous perdez votre mise.";
            saveGameResult();
            return true;
        }
        
        return false;
    }
    
    /**
     * Player action: Hit (take another card)
     * 
     * @return true if action successful, false if not allowed
     */
    public boolean hit() {
        if (state != GameState.PLAYER_TURN) {
            return false;
        }
        
        playerHand.addCard(deck.dealCard());
        
        // Check for bust
        if (playerHand.isBust()) {
            state = GameState.GAME_OVER;
            result = GameResult.LOSS;
            winnings = -currentBet;
            resultMessage = "Bust! Vous avez dépassé 21. Vous perdez " + currentBet + " crédits.";
            saveGameResult();
        }
        
        System.out.println("[VT-OS] Player hits. Hand value: " + playerHand.getValue());
        return true;
    }
    
    /**
     * Player action: Stand (end turn, let dealer play)
     * 
     * @return true if action successful
     */
    public boolean stand() {
        if (state != GameState.PLAYER_TURN) {
            return false;
        }
        
        state = GameState.DEALER_TURN;
        System.out.println("[VT-OS] Player stands. Dealer's turn.");
        
        // Execute dealer turn
        dealerTurn();
        
        return true;
    }
    
    /**
     * Player action: Double Down (double bet, take one card, then stand)
     * Only allowed on first two cards with sufficient balance.
     * 
     * @return true if action successful
     */
    public boolean doubleDown() {
        if (state != GameState.PLAYER_TURN) {
            return false;
        }
        
        // Can only double on first two cards
        if (playerHand.getCardCount() != 2) {
            resultMessage = "Vous ne pouvez doubler qu'avec 2 cartes.";
            return false;
        }
        
        // Check if player has enough balance
        if (currentBet > balance) {
            resultMessage = "Solde insuffisant pour doubler.";
            return false;
        }
        
        // Double the bet
        balance -= currentBet;
        currentBet *= 2;
        
        // Deal one card
        playerHand.addCard(deck.dealCard());
        
        System.out.println("[VT-OS] Player doubles down. New bet: " + currentBet);
        
        // Check for bust
        if (playerHand.isBust()) {
            state = GameState.GAME_OVER;
            result = GameResult.LOSS;
            winnings = -currentBet;
            resultMessage = "Bust après double! Vous perdez " + currentBet + " crédits.";
            saveGameResult();
        } else {
            // Otherwise, proceed to dealer turn
            state = GameState.DEALER_TURN;
            dealerTurn();
        }
        
        return true;
    }
    
    /**
     * Dealer's turn - draws until reaching threshold (≥17)
     */
    private void dealerTurn() {
        // Dealer reveals hidden card and draws according to rules
        // Must hit on 16 or less, stand on 17 or more
        while (dealerHand.getValue() < DEALER_HIT_THRESHOLD) {
            dealerHand.addCard(deck.dealCard());
        }
        
        System.out.println("[VT-OS] Dealer finishes. Hand value: " + dealerHand.getValue());
        
        // Determine winner
        determineWinner();
    }
    
    /**
     * Determine the winner and calculate payouts
     */
    private void determineWinner() {
        int playerValue = playerHand.getValue();
        int dealerValue = dealerHand.getValue();
        
        if (dealerHand.isBust()) {
            // Dealer busts, player wins
            result = GameResult.WIN;
            winnings = currentBet;
            balance += currentBet + winnings;
            resultMessage = "Le croupier bust! Vous gagnez " + winnings + " crédits!";
        } else if (playerValue > dealerValue) {
            // Player wins
            result = GameResult.WIN;
            winnings = currentBet;
            balance += currentBet + winnings;
            resultMessage = "Vous gagnez " + winnings + " crédits!";
        } else if (playerValue < dealerValue) {
            // Dealer wins
            result = GameResult.LOSS;
            winnings = -currentBet;
            resultMessage = "Le croupier gagne. Vous perdez " + currentBet + " crédits.";
        } else {
            // Push (tie)
            result = GameResult.PUSH;
            balance += currentBet; // Refund
            winnings = 0;
            resultMessage = "Égalité! Votre mise vous est remboursée.";
        }
        
        state = GameState.GAME_OVER;
        saveGameResult();
        
        System.out.println("[VT-OS] Result: " + result + " | " + resultMessage);
    }
    
    /**
     * Save game result to database
     */
    private void saveGameResult() {
        if (playerId > 0) {
            databaseManager.saveBlackjackGameResult(
                playerId,
                currentBet,
                result.name(),
                balance,
                playerHand.getValue(),
                dealerHand.getValue()
            );
            
            // Update player balance in database
            databaseManager.updateBlackjackPlayerBalance(playerId, balance);
        }
    }
    
    /**
     * Start a new round (keep player and balance)
     */
    public void newRound() {
        playerHand.clear();
        dealerHand.clear();
        currentBet = 0;
        result = null;
        resultMessage = "";
        winnings = 0;
        
        if (balance < MINIMUM_BET) {
            // Reset balance if too low (generous Vault-Tec casino!)
            balance = INITIAL_BALANCE;
            databaseManager.updateBlackjackPlayerBalance(playerId, balance);
            resultMessage = "Solde rechargé à " + INITIAL_BALANCE + " crédits.";
        }
        
        state = GameState.BETTING;
        
        System.out.println("[VT-OS] New round started. Balance: " + balance);
    }
    
    /**
     * Reset the entire game (new player session)
     */
    public void resetGame() {
        playerName = null;
        balance = INITIAL_BALANCE;
        playerId = -1;
        newRound();
        state = GameState.WAITING;
        
        System.out.println("[VT-OS] Game reset. Awaiting new player.");
    }
    
    // ==================== GETTERS ====================
    
    public Hand getPlayerHand() {
        return playerHand;
    }
    
    public Hand getDealerHand() {
        return dealerHand;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getBalance() {
        return balance;
    }
    
    public int getCurrentBet() {
        return currentBet;
    }
    
    public GameState getState() {
        return state;
    }
    
    public GameResult getResult() {
        return result;
    }
    
    public String getResultMessage() {
        return resultMessage;
    }
    
    public int getWinnings() {
        return winnings;
    }
    
    public int getMinimumBet() {
        return MINIMUM_BET;
    }
    
    public boolean canDouble() {
        return state == GameState.PLAYER_TURN 
            && playerHand.getCardCount() == 2 
            && currentBet <= balance;
    }
    
    /**
     * Check if player can afford minimum bet
     */
    public boolean canAffordMinimumBet() {
        return balance >= MINIMUM_BET;
    }
    
    @Override
    public String toString() {
        return "BlackjackGame{" +
                "player='" + playerName + '\'' +
                ", balance=" + balance +
                ", bet=" + currentBet +
                ", state=" + state +
                ", playerHand=" + playerHand.getValue() +
                ", dealerHand=" + dealerHand.getValue() +
                '}';
    }
}
