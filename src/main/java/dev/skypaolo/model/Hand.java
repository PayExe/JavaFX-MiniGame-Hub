package dev.skypaolo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Vault-Tec Model: Hand
 * Represents a hand of cards in Blackjack with proper value calculation.
 * Handles the special Ace rule (1 or 11) for optimal hand value.
 * 
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Optimal Ace value calculation
 * - Perception: Bust detection and Blackjack identification
 * 
 * Remember: The house always wins... eventually.
 */
public class Hand {
    
    private final List<Card> cards;
    private final String owner; // "Player" or "Dealer"
    
    /**
     * Constructor - Create an empty hand
     * 
     * @param owner Name of the hand owner (for display purposes)
     */
    public Hand(String owner) {
        this.cards = new ArrayList<>();
        this.owner = owner;
    }
    
    /**
     * Add a card to the hand
     * 
     * @param card The card to add
     */
    public void addCard(Card card) {
        if (card != null) {
            cards.add(card);
        }
    }
    
    /**
     * Add multiple cards to the hand
     * 
     * @param newCards List of cards to add
     */
    public void addCards(List<Card> newCards) {
        if (newCards != null) {
            cards.addAll(newCards);
        }
    }
    
    /**
     * Calculate the hand's value with optimal Ace handling.
     * Aces count as 11 unless that would cause a bust, then they count as 1.
     * 
     * @return The calculated hand value
     */
    public int getValue() {
        int value = 0;
        int aceCount = 0;
        
        // First pass: count all cards, treating Aces as 11
        for (Card card : cards) {
            value += card.getValue();
            if (card.isAce()) {
                aceCount++;
            }
        }
        
        // Second pass: convert Aces from 11 to 1 as needed to avoid busting
        while (value > 21 && aceCount > 0) {
            value -= 10; // Ace changes from 11 to 1 (difference of 10)
            aceCount--;
        }
        
        return value;
    }
    
    /**
     * Get the alternative value showing what Aces could be worth.
     * Used for display when Aces are present (e.g., "7/17" for Ace + 6).
     * 
     * @return Alternative value, or -1 if no alternative exists
     */
    public int getAlternativeValue() {
        int value = 0;
        int aceCount = 0;
        
        for (Card card : cards) {
            value += card.getValue();
            if (card.isAce()) {
                aceCount++;
            }
        }
        
        // If we have aces and we're counting them as 11 (value > 21 would already convert)
        // Return the "soft" value (with Ace as 11) if current calculation uses Ace as 1
        if (aceCount > 0 && value <= 21) {
            int softValue = value;
            // Check if we're in "hard" mode (Aces counted as 1)
            int hardAceCount = 0;
            int testValue = 0;
            for (Card card : cards) {
                if (card.isAce()) {
                    hardAceCount++;
                    testValue += 11;
                } else {
                    testValue += card.getValue();
                }
            }
            while (testValue > 21 && hardAceCount > 0) {
                testValue -= 10;
                hardAceCount--;
            }
            // If current value differs from test, we have alternatives
            if (testValue != value && testValue <= 21) {
                return testValue;
            }
        }
        
        return -1;
    }
    
    /**
     * Check if the hand is a bust (over 21)
     */
    public boolean isBust() {
        return getValue() > 21;
    }
    
    /**
     * Check if the hand is a Blackjack (21 with exactly 2 cards)
     */
    public boolean isBlackjack() {
        return cards.size() == 2 && getValue() == 21;
    }
    
    /**
     * Check if the hand is a soft hand (contains an Ace counted as 11)
     */
    public boolean isSoft() {
        if (!hasAce()) return false;
        
        int value = 0;
        int aceCount = 0;
        
        for (Card card : cards) {
            value += card.getValue();
            if (card.isAce()) {
                aceCount++;
            }
        }
        
        // If we're not forced to convert any Aces to 1, it's soft
        return value <= 21;
    }
    
    /**
     * Check if the hand contains at least one Ace
     */
    public boolean hasAce() {
        for (Card card : cards) {
            if (card.isAce()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the number of cards in the hand
     */
    public int getCardCount() {
        return cards.size();
    }
    
    /**
     * Check if the hand is empty
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
    
    /**
     * Clear all cards from the hand
     */
    public void clear() {
        cards.clear();
    }
    
    /**
     * Get a specific card from the hand
     * 
     * @param index Card index (0-based)
     * @return The card at that index, or null if out of bounds
     */
    public Card getCard(int index) {
        if (index >= 0 && index < cards.size()) {
            return cards.get(index);
        }
        return null;
    }
    
    /**
     * Get all cards in the hand
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }
    
    /**
     * Get the first card (useful for dealer's hidden card scenario)
     */
    public Card getFirstCard() {
        return cards.isEmpty() ? null : cards.get(0);
    }
    
    /**
     * Get the owner name
     */
    public String getOwner() {
        return owner;
    }
    
    /**
     * Get a formatted display of the hand value
     * Shows alternative value if Aces are present (e.g., "7 ou 17")
     */
    public String getValueDisplay() {
        int value = getValue();
        int altValue = getAlternativeValue();
        
        if (altValue > 0 && altValue != value) {
            return Math.min(value, altValue) + " ou " + Math.max(value, altValue);
        }
        return String.valueOf(value);
    }
    
    /**
     * Get a string representation of all cards in the hand
     */
    public String getCardsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append(cards.get(i).getDisplayString());
            if (i < cards.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return owner + "'s Hand{" +
                "cards=" + cards.size() +
                ", value=" + getValue() +
                ", blackjack=" + isBlackjack() +
                ", bust=" + isBust() +
                '}';
    }
}
