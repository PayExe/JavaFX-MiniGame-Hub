package dev.skypaolo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Vault-Tec Model: Deck
 * Represents a standard 52-card playing deck with shuffle capability.
 * 
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Proper deck initialization and management
 * - Luck: Randomized shuffling for fair play
 * 
 * "A well-shuffled deck is the foundation of fair play." - Vault-Tec Gaming Division
 */
public class Deck {
    
    private static final int DECK_SIZE = 52;
    
    private List<Card> cards;
    private int currentIndex;
    
    /**
     * Constructor - Create and shuffle a new deck
     */
    public Deck() {
        initializeDeck();
        shuffle();
    }
    
    /**
     * Initialize a fresh 52-card deck
     * Vault-Tec certified standard deck composition
     */
    private void initializeDeck() {
        cards = new ArrayList<>(DECK_SIZE);
        
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        
        currentIndex = 0;
        System.out.println("[VT-OS] Deck initialized: 52 cards ready for play.");
    }
    
    /**
     * Shuffle the deck using Collections.shuffle()
     * Vault-Tec Randomization Protocol v2.077
     */
    public void shuffle() {
        Collections.shuffle(cards);
        currentIndex = 0;
        System.out.println("[VT-OS] Deck shuffled. Randomization: NOMINAL.");
    }
    
    /**
     * Deal a single card from the top of the deck
     * 
     * @return The dealt card, or null if deck is empty
     */
    public Card dealCard() {
        if (currentIndex >= cards.size()) {
            System.err.println("[VT-OS WARNING] Deck empty! Reshuffling...");
            shuffle();
            currentIndex = 0;
        }
        
        Card dealtCard = cards.get(currentIndex);
        currentIndex++;
        
        return dealtCard;
    }
    
    /**
     * Deal multiple cards at once
     * 
     * @param count Number of cards to deal
     * @return List of dealt cards
     */
    public List<Card> dealCards(int count) {
        List<Card> dealtCards = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Card card = dealCard();
            if (card != null) {
                dealtCards.add(card);
            }
        }
        
        return dealtCards;
    }
    
    /**
     * Check if the deck needs reshuffling
     * Vault-Tec recommends reshuffling when less than 25% of deck remains
     * 
     * @return true if deck should be reshuffled
     */
    public boolean needsReshuffle() {
        return getCardsRemaining() < (DECK_SIZE / 4);
    }
    
    /**
     * Get the number of cards remaining in the deck
     */
    public int getCardsRemaining() {
        return DECK_SIZE - currentIndex;
    }
    
    /**
     * Get the total deck size (always 52)
     */
    public int getDeckSize() {
        return DECK_SIZE;
    }
    
    /**
     * Check if the deck is empty
     */
    public boolean isEmpty() {
        return currentIndex >= DECK_SIZE;
    }
    
    /**
     * Reset and reshuffle the deck
     */
    public void reset() {
        initializeDeck();
        shuffle();
    }
    
    /**
     * Peek at the next card without dealing it
     * For debugging/testing purposes only
     * Vault-Tec does not condone cheating!
     */
    public Card peekNextCard() {
        if (currentIndex < cards.size()) {
            return cards.get(currentIndex);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Deck{" +
                "cardsRemaining=" + getCardsRemaining() +
                ", currentIndex=" + currentIndex +
                '}';
    }
}
