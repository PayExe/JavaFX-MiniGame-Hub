package dev.skypaolo.model;

/**
 * Vault-Tec Enumeration: Guess Result Types
 * Represents the possible outcomes of a guess in the Plus ou Moins game.
 * 
 * S.P.E.C.I.A.L. Stats: Perception (categorization)
 */
public enum GuessResult {
    /**
     * The guessed number is higher than the secret number
     * French: "C'est plus petit!"
     */
    HIGHER,
    
    /**
     * The guessed number is lower than the secret number
     * French: "C'est plus grand!"
     */
    LOWER,
    
    /**
     * The guessed number matches the secret number
     * French: "Bravo! Nombre trouvé!"
     */
    CORRECT,
    
    /**
     * The guess is invalid (out of range or not a number)
     * French: "Entrée invalide"
     */
    INVALID,
    
    /**
     * Game is already over, no more guesses allowed
     * French: "Partie terminée"
     */
    GAME_OVER
}
