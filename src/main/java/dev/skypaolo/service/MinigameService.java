package dev.skypaolo.service;

import dev.skypaolo.model.Minigame;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MinigameService {

  private static MinigameService instance;
  private final ObservableList<Minigame> minigames;

  private MinigameService() {
    this.minigames = FXCollections.observableArrayList();
    initializeExampleMinigames();
  }

  /**
   * Singleton pattern - Vault-Tec approved resource management
   */
  public static MinigameService getInstance() {
    if (instance == null) {
      instance = new MinigameService();
    }
    return instance;
  }

  /**
   * Initialize with minigames
   * Vault-Tec recommends: Always have sample data for demonstration purposes!
   */
  private void initializeExampleMinigames() {
    // Game 1: Plus ou Moins (Implemented per specification)
    minigames.add(
      new Minigame(
        "plus-ou-moins",
        "Jeu du + ou -",
        "Devinez le nombre secret entre 1 et 1000! Une simulation de déduction numérique approuvée par Vault-Tec. 10 tentatives maximum pour trouver le nombre mystère.",
        "/images/placeholders/plusoumoins.jpg"
      )
    );
    minigames.add(
      new Minigame(
        "true-or-false",
        "True or False",
        "Réponds vrai ou faux aux questions affichées une par une.",
        "/images/placeholders/vouf.jpg"
      )
    );

    // Game 3: Snake (Implemented per specification)
    minigames.add(
      new Minigame(
        "snake",
        "Snake",
        "Contrôlez un serpent qui grandit en mangeant de la nourriture. " +
          "Évitez les murs et votre propre queue! " +
          "Difficulté: Difficile | Points: 6",
        "/images/placeholders/snake.jpg"
      )
    );

    // Game 4: Blackjack (Implemented per specification)
    minigames.add(
      new Minigame(
        "blackjack",
        "Blackjack",
        "Le classique jeu de cartes 21! Affrontez le croupier Vault-Tec. " +
          "Misez vos crédits, tirez des cartes, atteignez 21 sans dépasser. " +
          "Blackjack naturel = ×1.5 la mise! " +
          "Difficulté: Moyen | Points: 4",
        "/images/placeholders/blackjack.jpg"
      )
    );
  }

  /**
   * Get all available minigames
   */
  public ObservableList<Minigame> getAllMinigames() {
    return minigames;
  }

  /**
   * Get a minigame by ID
   */
  public Minigame getMinigameById(String id) {
    return minigames
      .stream()
      .filter(m -> m.getId().equals(id))
      .findFirst()
      .orElse(null);
  }

  /**
   * Add a new minigame (for dynamic expansion)
   */
  public void addMinigame(Minigame minigame) {
    minigames.add(minigame);
  }

  /**
   * Get minigame count
   */
  public int getMinigameCount() {
    return minigames.size();
  }
}
