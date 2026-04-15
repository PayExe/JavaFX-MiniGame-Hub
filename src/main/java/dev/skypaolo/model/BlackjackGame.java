package dev.skypaolo.model;

import dev.skypaolo.database.DatabaseManager;

public class BlackjackGame {
    
    private static final int INITIAL_BALANCE = 1000;
    private static final int MINIMUM_BET = 10;
    private static final int DEALER_HIT_THRESHOLD = 17;
    private static final double BLACKJACK_PAYOUT_RATIO = 1.5;
    
    public enum GameState {
        WAITING,
        BETTING,
        PLAYER_TURN,
        DEALER_TURN,
        GAME_OVER
    }
    
    public enum GameResult {
        WIN,
        LOSS,
        PUSH,
        BLACKJACK_WIN,
        DEALER_BLACKJACK
    }
    
    private Deck deck;
    private Hand playerHand;
    private Hand dealerHand;
    
    private String playerName;
    private int balance;
    private int currentBet;
    
    private GameState state;
    private GameResult result;
    private String resultMessage;
    private int winnings;
    
    private DatabaseManager databaseManager;
    private int playerId;
    
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
    
    public void registerPlayer(String playerName) {
        this.playerName = playerName;
        this.playerId = databaseManager.getOrCreateBlackjackPlayerId(playerName);
        int savedBalance = databaseManager.getBlackjackPlayerBalance(playerId);
        this.balance = savedBalance > 0 ? savedBalance : INITIAL_BALANCE;
        this.state = GameState.BETTING;
        
        System.out.println("[VT-OS] Player registered: " + playerName + " | Balance: " + balance);
    }
    
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
        this.balance -= betAmount;
        
        dealInitialCards();
        
        if (checkForBlackjacks()) {
            state = GameState.GAME_OVER;
        } else {
            state = GameState.PLAYER_TURN;
        }
        
        System.out.println("[VT-OS] Bet placed: " + betAmount + " | Balance: " + balance);
        return true;
    }
    
    private void dealInitialCards() {
        playerHand.clear();
        dealerHand.clear();
        
        if (deck.needsReshuffle()) {
            deck.reset();
        }
        
        playerHand.addCard(deck.dealCard());
        dealerHand.addCard(deck.dealCard());
        playerHand.addCard(deck.dealCard());
        dealerHand.addCard(deck.dealCard());
        
        System.out.println("[VT-OS] Cards dealt. Player: " + playerHand.getValue() + 
                          " | Dealer shows: " + dealerHand.getFirstCard().getDisplayString());
    }
    
    private boolean checkForBlackjacks() {
        boolean playerHasBlackjack = playerHand.isBlackjack();
        boolean dealerHasBlackjack = dealerHand.isBlackjack();
        
        if (playerHasBlackjack && dealerHasBlackjack) {
            result = GameResult.PUSH;
            resultMessage = "Double Blackjack! Égalité.";
            balance += currentBet;
            winnings = 0;
            saveGameResult();
            return true;
        } else if (playerHasBlackjack) {
            result = GameResult.BLACKJACK_WIN;
            winnings = (int) (currentBet * BLACKJACK_PAYOUT_RATIO);
            balance += currentBet + winnings;
            resultMessage = "Blackjack! Vous gagnez " + winnings + " crédits!";
            saveGameResult();
            return true;
        } else if (dealerHasBlackjack) {
            result = GameResult.DEALER_BLACKJACK;
            winnings = -currentBet;
            resultMessage = "Le croupier a un Blackjack! Vous perdez votre mise.";
            saveGameResult();
            return true;
        }
        
        return false;
    }
    
    public boolean hit() {
        if (state != GameState.PLAYER_TURN) {
            return false;
        }
        
        playerHand.addCard(deck.dealCard());
        
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
    
    public boolean stand() {
        if (state != GameState.PLAYER_TURN) {
            return false;
        }
        
        state = GameState.DEALER_TURN;
        System.out.println("[VT-OS] Player stands. Dealer's turn.");
        dealerTurn();
        return true;
    }
    
    public boolean doubleDown() {
        if (state != GameState.PLAYER_TURN) {
            return false;
        }
        
        if (playerHand.getCardCount() != 2) {
            resultMessage = "Vous ne pouvez doubler qu'avec 2 cartes.";
            return false;
        }
        
        if (currentBet > balance) {
            resultMessage = "Solde insuffisant pour doubler.";
            return false;
        }
        
        balance -= currentBet;
        currentBet *= 2;
        playerHand.addCard(deck.dealCard());
        
        System.out.println("[VT-OS] Player doubles down. New bet: " + currentBet);
        
        if (playerHand.isBust()) {
            state = GameState.GAME_OVER;
            result = GameResult.LOSS;
            winnings = -currentBet;
            resultMessage = "Bust après double! Vous perdez " + currentBet + " crédits.";
            saveGameResult();
        } else {
            state = GameState.DEALER_TURN;
            dealerTurn();
        }
        
        return true;
    }
    
    private void dealerTurn() {
        while (dealerHand.getValue() < DEALER_HIT_THRESHOLD) {
            dealerHand.addCard(deck.dealCard());
        }
        
        System.out.println("[VT-OS] Dealer finishes. Hand value: " + dealerHand.getValue());
        determineWinner();
    }
    
    private void determineWinner() {
        int playerValue = playerHand.getValue();
        int dealerValue = dealerHand.getValue();
        
        if (dealerHand.isBust()) {
            result = GameResult.WIN;
            winnings = currentBet;
            balance += currentBet + winnings;
            resultMessage = "Le croupier bust! Vous gagnez " + winnings + " crédits!";
        } else if (playerValue > dealerValue) {
            result = GameResult.WIN;
            winnings = currentBet;
            balance += currentBet + winnings;
            resultMessage = "Vous gagnez " + winnings + " crédits!";
        } else if (playerValue < dealerValue) {
            result = GameResult.LOSS;
            winnings = -currentBet;
            resultMessage = "Le croupier gagne. Vous perdez " + currentBet + " crédits.";
        } else {
            result = GameResult.PUSH;
            balance += currentBet;
            winnings = 0;
            resultMessage = "Égalité! Votre mise vous est remboursée.";
        }
        
        state = GameState.GAME_OVER;
        saveGameResult();
        
        System.out.println("[VT-OS] Result: " + result + " | " + resultMessage);
    }
    
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
            databaseManager.updateBlackjackPlayerBalance(playerId, balance);
        }
    }
    
    public void newRound() {
        playerHand.clear();
        dealerHand.clear();
        currentBet = 0;
        result = null;
        resultMessage = "";
        winnings = 0;
        
        if (balance < MINIMUM_BET) {
            balance = INITIAL_BALANCE;
            databaseManager.updateBlackjackPlayerBalance(playerId, balance);
            resultMessage = "Solde rechargé à " + INITIAL_BALANCE + " crédits.";
        }
        
        state = GameState.BETTING;
        
        System.out.println("[VT-OS] New round started. Balance: " + balance);
    }
    
    public void resetGame() {
        playerName = null;
        balance = INITIAL_BALANCE;
        playerId = -1;
        newRound();
        state = GameState.WAITING;
        
        System.out.println("[VT-OS] Game reset. Awaiting new player.");
    }
    
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
