package dev.skypaolo.model;

/**
 * Vault-Tec Model: Card
 * Represents a playing card in the Blackjack simulation.
 * 
 * S.P.E.C.I.A.L. Stats:
 * - Intelligence: Value calculation with rank mapping
 * - Perception: Suit and rank identification
 * 
 * Vault-Tec is not responsible for card counting techniques employed by players.
 */
public class Card {
    
    /**
     * Card suits - Vault-Tec standard playing card specification
     */
    public enum Suit {
        HEARTS("♥", "Rouge"),
        DIAMONDS("♦", "Rouge"),
        CLUBS("♣", "Noir"),
        SPADES("♠", "Noir");
        
        private final String symbol;
        private final String color;
        
        Suit(String symbol, String color) {
            this.symbol = symbol;
            this.color = color;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    /**
     * Card ranks - Standard 13 ranks per suit
     */
    public enum Rank {
        ACE("A", 11),
        TWO("2", 2),
        THREE("3", 3),
        FOUR("4", 4),
        FIVE("5", 5),
        SIX("6", 6),
        SEVEN("7", 7),
        EIGHT("8", 8),
        NINE("9", 9),
        TEN("10", 10),
        JACK("J", 10),
        QUEEN("Q", 10),
        KING("K", 10);
        
        private final String display;
        private final int value;
        
        Rank(String display, int value) {
            this.display = display;
            this.value = value;
        }
        
        public String getDisplay() {
            return display;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    private final Suit suit;
    private final Rank rank;
    
    /**
     * Constructor - Create a new card
     * 
     * @param suit The card suit
     * @param rank The card rank
     */
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
    
    /**
     * Get the card's suit
     */
    public Suit getSuit() {
        return suit;
    }
    
    /**
     * Get the card's rank
     */
    public Rank getRank() {
        return rank;
    }
    
    /**
     * Get the card's value (for Blackjack scoring)
     * Note: Ace returns 11 - hand calculation handles the 1/11 logic
     */
    public int getValue() {
        return rank.getValue();
    }
    
    /**
     * Check if this card is an Ace
     */
    public boolean isAce() {
        return rank == Rank.ACE;
    }
    
    /**
     * Check if this is a face card (Jack, Queen, King)
     */
    public boolean isFaceCard() {
        return rank == Rank.JACK || rank == Rank.QUEEN || rank == Rank.KING;
    }
    
    /**
     * Get the card's color
     */
    public String getColor() {
        return suit.getColor();
    }
    
    /**
     * Check if this card is red
     */
    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }
    
    /**
     * Check if this card is black
     */
    public boolean isBlack() {
        return suit == Suit.CLUBS || suit == Suit.SPADES;
    }
    
    /**
     * Get display string for the card
     */
    public String getDisplayString() {
        return rank.getDisplay() + suit.getSymbol();
    }
    
    @Override
    public String toString() {
        return rank + " of " + suit + " (" + getDisplayString() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return suit == card.suit && rank == card.rank;
    }
    
    @Override
    public int hashCode() {
        return 31 * suit.hashCode() + rank.hashCode();
    }
}
