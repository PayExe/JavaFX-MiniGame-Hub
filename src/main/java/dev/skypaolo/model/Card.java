package dev.skypaolo.model;

public class Card {
    
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
    
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
    
    public Suit getSuit() {
        return suit;
    }
    
    public Rank getRank() {
        return rank;
    }
    
    public int getValue() {
        return rank.getValue();
    }
    
    public boolean isAce() {
        return rank == Rank.ACE;
    }
    
    public boolean isFaceCard() {
        return rank == Rank.JACK || rank == Rank.QUEEN || rank == Rank.KING;
    }
    
    public String getColor() {
        return suit.getColor();
    }
    
    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }
    
    public boolean isBlack() {
        return suit == Suit.CLUBS || suit == Suit.SPADES;
    }
    
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
