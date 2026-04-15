package dev.skypaolo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    
    private static final int DECK_SIZE = 52;
    
    private List<Card> cards;
    private int currentIndex;
    
    public Deck() {
        initializeDeck();
        shuffle();
    }
    
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
    
    public void shuffle() {
        Collections.shuffle(cards);
        currentIndex = 0;
        System.out.println("[VT-OS] Deck shuffled. Randomization: NOMINAL.");
    }
    
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
    
    public boolean needsReshuffle() {
        return getCardsRemaining() < (DECK_SIZE / 4);
    }
    
    public int getCardsRemaining() {
        return DECK_SIZE - currentIndex;
    }
    
    public int getDeckSize() {
        return DECK_SIZE;
    }
    
    public boolean isEmpty() {
        return currentIndex >= DECK_SIZE;
    }
    
    public void reset() {
        initializeDeck();
        shuffle();
    }
    
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
