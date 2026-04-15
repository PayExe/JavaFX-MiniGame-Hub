package dev.skypaolo.model;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    
    private final List<Card> cards;
    private final String owner;
    
    public Hand(String owner) {
        this.cards = new ArrayList<>();
        this.owner = owner;
    }
    
    public void addCard(Card card) {
        if (card != null) {
            cards.add(card);
        }
    }
    
    public void addCards(List<Card> newCards) {
        if (newCards != null) {
            cards.addAll(newCards);
        }
    }
    
    public int getValue() {
        int value = 0;
        int aceCount = 0;
        
        for (Card card : cards) {
            value += card.getValue();
            if (card.isAce()) {
                aceCount++;
            }
        }
        
        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }
        
        return value;
    }
    
    public int getAlternativeValue() {
        int value = 0;
        int aceCount = 0;
        
        for (Card card : cards) {
            value += card.getValue();
            if (card.isAce()) {
                aceCount++;
            }
        }
        
        if (aceCount > 0 && value <= 21) {
            int softValue = value;
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
            if (testValue != value && testValue <= 21) {
                return testValue;
            }
        }
        
        return -1;
    }
    
    public boolean isBust() {
        return getValue() > 21;
    }
    
    public boolean isBlackjack() {
        return cards.size() == 2 && getValue() == 21;
    }
    
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
        
        return value <= 21;
    }
    
    public boolean hasAce() {
        for (Card card : cards) {
            if (card.isAce()) {
                return true;
            }
        }
        return false;
    }
    
    public int getCardCount() {
        return cards.size();
    }
    
    public boolean isEmpty() {
        return cards.isEmpty();
    }
    
    public void clear() {
        cards.clear();
    }
    
    public Card getCard(int index) {
        if (index >= 0 && index < cards.size()) {
            return cards.get(index);
        }
        return null;
    }
    
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }
    
    public Card getFirstCard() {
        return cards.isEmpty() ? null : cards.get(0);
    }
    
    public String getOwner() {
        return owner;
    }
    
    public String getValueDisplay() {
        int value = getValue();
        int altValue = getAlternativeValue();
        
        if (altValue > 0 && altValue != value) {
            return Math.min(value, altValue) + " ou " + Math.max(value, altValue);
        }
        return String.valueOf(value);
    }
    
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
