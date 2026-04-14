package dev.skypaolo.service;

import dev.skypaolo.model.Minigame;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Vault-Tec Service: Minigame Data Provider
 * Provides mini game data for the hub interface.
 * S.P.E.C.I.A.L. Stats: Intelligence (data management), Luck (variety)
 */
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
                "/images/placeholders/plus-ou-moins-placeholder.png"
            )
        );
        minigames.add(
            new Minigame(
                "true-or-false",
                "True or False",
                "Réponds vrai ou faux aux questions affichées une par une.",
                "/images/placeholders/true-or-false-placeholder.png"
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
