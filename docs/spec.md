# Mini-Games JavaFX — Specifications

> **Stack commun :** Java + JavaFX · SQLite (via `sqlite-jdbc`) · Architecture MVC · CSS styling

---

## Table des matières

1. [Jeu du + ou -](#1-jeu-du---ou---)
2. [True or False](#2-true-or-false)
3. [Pendu](#3-pendu)
4. [Memory](#4-memory)
5. [BlackJack](#5-blackjack)
6. [Sudoku](#6-sudoku)
7. [2048](#7-2048)
8. [Snake](#8-snake)
9. [Flappy Bird](#9-flappy-bird)
10. [PacMan](#10-pacman)

---

## 1. Jeu du + ou -
**Points :** 1 · **Difficulté :** Facile

### Résumé
Le joueur doit deviner un nombre secret entre 1 et 1000 en un nombre limité de tentatives. Après chaque essai, le jeu indique si le nombre cherché est plus grand ou plus petit.

### Modèle (Model)
- `int secretNumber` — généré aléatoirement à chaque partie
- `int attempts` — compteur de tentatives
- `int maxAttempts` — limite (ex. 10)
- `int score` — calculé à partir des tentatives restantes

### Vue (View — FXML)
- `Label` instructions + indications (« plus grand », « plus petit », « bravo »)
- `TextField` saisie du nombre
- `Button` « Valider »
- `Label` score et tentatives restantes

### Contrôleur (Controller)
- Validation saisie (entier, dans \[1–1000\])
- Comparaison avec `secretNumber`
- Mise à jour du score et des labels
- Déclenchement d'une nouvelle partie

### Base de données
- Table `scores` : `id`, `player_name`, `score`, `attempts_used`, `date`

### Règles métier
- Score = `(maxAttempts - attempts) * 10`
- Partie terminée si nombre trouvé **ou** `attempts >= maxAttempts`

---

## 2. True or False
**Points :** 3 · **Difficulté :** Facile

### Résumé
Le joueur répond « Vrai » ou « Faux » à des questions affichées une par une. Les questions sont tirées aléatoirement depuis la base de données. Le joueur peut aussi ajouter ses propres questions.

### Modèle
- `Question` : `id`, `text`, `answer (boolean)`, `category`
- `int score`, `int questionIndex`, `List<Question> queue`

### Vue
- `Label` texte de la question
- `Button` « Vrai » / `Button` « Faux »
- Feedback visuel : fond vert/rouge + animation `FadeTransition`
- `Label` score
- Formulaire d'ajout : `TextField` question + `ToggleButton` Vrai/Faux + `Button` « Ajouter »

### Contrôleur
- Chargement des questions (ordre aléatoire)
- Vérification réponse → mise à jour score + feedback
- Passage à la question suivante après délai (`PauseTransition`)
- Persistance d'une nouvelle question en base

### Base de données
- Table `questions` : `id`, `text`, `answer`, `category`
- Table `scores` : `id`, `player_name`, `score`, `total_questions`, `date`

### Règles métier
- +1 point par bonne réponse
- Partie sur N questions (configurable, ex. 10)
- Score final affiché en fin de partie

---

## 3. Pendu
**Points :** 4 · **Difficulté :** Moyen

### Résumé
Le joueur devine un mot lettre par lettre. Chaque mauvaise lettre ajoute un élément au dessin du pendu. Le jeu se termine quand le mot est trouvé ou quand le pendu est complet (6 erreurs).

### Modèle
- `String secretWord` — tiré aléatoirement en base
- `char[] revealed` — lettres découvertes
- `Set<Character> wrongLetters`
- `int errorsLeft` (max 6)

### Vue
- `Canvas` — dessin progressif du pendu via `GraphicsContext`
- `HBox` de `Label` — affichage `_ _ _ _`
- Clavier virtuel : `GridPane` de `Button` A–Z
- `FlowPane` lettres incorrectes déjà tentées
- `Label` score
- Formulaire ajout de mot : `TextField` + `Button`

### Contrôleur
- Sélection d'un mot aléatoire en base
- Traitement d'un clic lettre : correct → révèle / incorrect → dessine + incrément erreur
- Désactivation du bouton lettre après usage
- Détection victoire/défaite

### Base de données
- Table `words` : `id`, `word`, `category`, `difficulty`
- Table `scores` : `id`, `player_name`, `score`, `word`, `errors`, `date`

### Règles métier
- Score = `errorsLeft * 10`
- 6 segments à dessiner (tête, corps, bras×2, jambe×2)
- Victoire si toutes les lettres révélées avant 6 erreurs

---

## 4. Memory
**Points :** 4 · **Difficulté :** Moyen

### Résumé
Plateau de cartes retournées face cachée. Le joueur retourne deux cartes à la fois ; si elles forment une paire, elles restent visibles. Objectif : trouver toutes les paires en un minimum de coups.

### Modèle
- `Card` : `id`, `pairId`, `imagePath/symbol`, `isRevealed`, `isMatched`
- `int moves`, `long startTime`
- `List<Card> board` — mélangé aléatoirement

### Vue
- `GridPane` 4×4 (ou configurable) — chaque cellule = `Button` ou `StackPane`
- `ImageView` ou `Label` symbole (caché par défaut)
- Animations : `RotateTransition` (retournement), `PauseTransition` (délai avant ré-masquage)
- `Label` coups + chronomètre (`Timeline`)

### Contrôleur
- Gestion de l'état « première carte / deuxième carte »
- Comparaison des paires
- Blocage des clics pendant l'animation
- Détection de fin de partie (toutes les paires trouvées)

### Base de données
- Table `cards` : `id`, `pair_id`, `image_path`, `symbol`
- Table `scores` : `id`, `player_name`, `moves`, `time_seconds`, `date`

### Règles métier
- Score = `1000 / (moves + time_seconds)` (formule indicative)
- Minimum 8 paires (16 cartes)

---

## 5. BlackJack
**Points :** 4 · **Difficulté :** Moyen

### Résumé
Implémentation du BlackJack classique avec gestion d'un compte joueur, argent virtuel et mises. Le joueur joue contre le croupier (IA).

### Modèle
- `Card` : `suit`, `rank`, `value`
- `Deck` : 52 cartes, mélangées
- `Hand` : liste de cartes + calcul valeur (As = 1 ou 11)
- `Player` : `name`, `balance`, `currentBet`

### Vue
- `BorderPane` table de jeu
- `HBox` main joueur / main croupier avec `ImageView` par carte
- `Button` Tirer (Hit) / Rester (Stand) / Doubler / Nouvelle Partie
- `TextField` mise + `Button` Miser
- `Label` solde, résultat du tour
- Animations : `TranslateTransition` distribution des cartes

### Contrôleur
- Logique distribution initiale (2 cartes chacun, 1 croupier cachée)
- Hit/Stand/Double
- Tour du croupier : tire jusqu'à ≥ 17
- Calcul gain/perte, mise à jour solde
- Persistance du compte joueur

### Base de données
- Table `players` : `id`, `name`, `balance`
- Table `history` : `id`, `player_id`, `bet`, `result`, `balance_after`, `date`

### Règles métier
- BlackJack naturel = ×1.5 la mise
- Bust (> 21) = perte automatique
- Égalité = remboursement de la mise
- Solde minimum pour jouer : 1

---

## 6. Sudoku
**Points :** 5 (+2 bonus générateur) · **Difficulté :** Moyen-Difficile

### Résumé
Grille 9×9 à compléter. Chaque ligne, colonne et bloc 3×3 doit contenir les chiffres 1 à 9 sans répétition. Validation en temps réel avec surlignage des erreurs.

### Modèle
- `int[9][9] solution` — grille complète
- `int[9][9] puzzle` — grille avec cases vides (0)
- `boolean[9][9] editable` — cases saisissables
- `int moves`, `long startTime`
- **(Bonus)** `SudokuGenerator` — algorithme backtracking

### Vue
- `GridPane` 9×9 de `TextField` (1 chiffre max)
- CSS : bordures épaisses pour délimiter les blocs 3×3, fond rouge sur erreur
- `Label` timer (`Timeline` 1 s) + score
- `Button` Vérifier / Nouvelle Partie / Indice

### Contrôleur
- Chargement puzzle (depuis BDD ou générateur)
- Validation en temps réel à chaque saisie (`TextFormatter`)
- Surlignage des conflits via CSS pseudo-class
- Détection de grille complète et valide

### Base de données
- Table `grids` : `id`, `puzzle` (JSON/string 81 chars), `solution`, `difficulty`
- Table `scores` : `id`, `player_name`, `time_seconds`, `moves`, `difficulty`, `date`

### Règles métier
- Saisie limitée aux chiffres 1–9
- Score = `max(0, 10000 - time_seconds * 10 - moves * 5)`
- (Bonus) Générateur : remplissage backtracking + retrait de cases selon difficulté

---

## 7. 2048
**Points :** 6 · **Difficulté :** Difficile

### Résumé
Grille 4×4 de tuiles numérotées. Le joueur déplace toutes les tuiles dans une direction ; les tuiles de même valeur fusionnent. Objectif : atteindre la tuile 2048.

### Modèle
- `int[4][4] grid`
- `int score`
- Logique déplacement+fusion pour les 4 directions
- Détection victoire (tuile 2048) et défaite (grille pleine, aucune fusion possible)

### Vue
- `GridPane` 4×4 de `Label` stylés dynamiquement par CSS selon valeur
- `Scene.setOnKeyPressed` — flèches directionnelles
- `ScaleTransition` apparition nouvelle tuile
- `Label` score + meilleur score

### Contrôleur
- Gestion des touches → appel logique déplacement
- Ajout d'une tuile aléatoire (2 ou 4) après chaque coup valide
- Mise à jour CSS class selon valeur tuile
- Sauvegarde du meilleur score

### Base de données
- Table `scores` : `id`, `player_name`, `score`, `max_tile`, `date`

### Règles métier
- Nouvelle tuile : 90 % chance d'un 2, 10 % d'un 4
- Fusion : deux tuiles identiques adjacentes → une tuile de valeur double, +valeur au score
- Victoire : tuile ≥ 2048 (le joueur peut continuer)
- Défaite : aucun mouvement possible

---

## 8. Snake
**Points :** 6 · **Difficulté :** Difficile

### Résumé
Le joueur contrôle un serpent qui grandit en mangeant de la nourriture. Le jeu se termine si le serpent heurte un mur ou sa propre queue.

### Modèle
- `Deque<Point> body` — segments du serpent
- `Point food` — position aléatoire
- `Direction currentDir`
- `int score`, `int speed`

### Vue
- `Canvas` — rendu via `GraphicsContext`
- `AnimationTimer` — boucle de jeu (tick toutes les N ms)
- `Scene.setOnKeyPressed` — touches directionnelles
- `Label` score + écran accueil / game over

### Contrôleur
- Mise à jour position à chaque tick
- Détection collision mur / queue
- Spawn nourriture (position libre aléatoire)
- Accélération progressive (optionnel)
- Persistance des scores

### Base de données
- Table `players` : `id`, `name`
- Table `scores` : `id`, `player_id`, `score`, `date`

### Règles métier
- +10 points par nourriture mangée
- Chaque nourriture mangée = +1 segment
- Collision = game over, affichage score final + scoreboard

---

## 9. Flappy Bird
**Points :** 7 · **Difficulté :** Difficile

### Résumé
L'oiseau tombe sous l'effet de la gravité. Le joueur appuie sur une touche/clic pour lui faire gagner de l'altitude. Des tuyaux verticaux à espacement aléatoire défilent de droite à gauche.

### Modèle
- `Bird` : `double y`, `double velocity`
- `List<Pipe>` : `x`, `gapY`, `gapHeight`, `passed`
- `int score`, `boolean gameRunning`
- Constantes : `GRAVITY`, `FLAP_FORCE`, `PIPE_SPEED`, `PIPE_INTERVAL`

### Vue
- `Canvas` + `AnimationTimer`
- `Scene.setOnKeyPressed` (espace) + `setOnMouseClicked` → flap
- `ImageView` ou dessin `GraphicsContext` pour l'oiseau et les tuyaux
- `Label` score + écran start / game over

### Contrôleur
- Mise à jour physique à chaque frame : `velocity += GRAVITY`, `y += velocity`
- Génération d'un nouveau tuyau à intervalle régulier
- Détection collision oiseau–tuyau et oiseau–sol
- Incrément score quand un tuyau est dépassé
- Persistance scores + scoreboard

### Base de données
- Table `players` : `id`, `name`
- Table `scores` : `id`, `player_id`, `score`, `date`

### Règles métier
- Flap : `velocity = -FLAP_FORCE`
- Mort si `y < 0` (plafond), `y > CANVAS_HEIGHT` (sol), ou collision tuyau
- Score = nombre de tuyaux passés

---

## 10. PacMan
**Points :** 7 · **Difficulté :** Difficile

### Résumé
PacMan se déplace dans un labyrinthe, mange des points et évite les fantômes. Les super-points lui permettent temporairement de chasser les fantômes.

### Modèle
- `int[][] maze` — matrice (0=vide, 1=mur, 2=point, 3=super-point)
- `PacMan` : `x`, `y`, `direction`, `lives`
- `List<Ghost>` : `x`, `y`, `mode (CHASE/FRIGHTENED/EATEN)`
- `int score`, `int dotsRemaining`

### Vue
- `Canvas` — labyrinthe et personnages via `GraphicsContext`
- `AnimationTimer` — boucle de jeu
- `Scene.setOnKeyPressed` — touches directionnelles
- `Label` score + vies (icônes) + écran game over / victoire

### Contrôleur
- Déplacement PacMan (tile par tile ou continu)
- Collision mur → bloqué
- Collecte point → score++, super-point → mode FRIGHTENED
- IA fantômes : CHASE (pathfinding simple vers PacMan) / FRIGHTENED (aléatoire)
- Collision PacMan–fantôme : FRIGHTENED → fantôme mangé / sinon → vie perdue
- Fin de partie : toutes les vies perdues ou tous les points collectés

### Base de données
- Table `players` : `id`, `name`
- Table `scores` : `id`, `player_id`, `score`, `level`, `date`

### Règles métier
- Point = +10, super-point = +50
- Fantôme mangé = +200 (×2 pour chaque fantôme consécutif)
- FRIGHTENED dure 7 secondes
- 3 vies au départ
- Victoire si `dotsRemaining == 0`

---

## Récapitulatif

| Jeu | Points | Difficulté | DB | Animations | Boucle de jeu |
|---|---|---|---|---|---|
| + ou - | 1 | Facile | scores | Non | Non |
| True or False | 3 | Facile | questions, scores | FadeTransition | Non |
| Pendu | 4 | Moyen | words, scores | Canvas | Non |
| Memory | 4 | Moyen | cards, scores | RotateTransition | Non |
| BlackJack | 4 | Moyen | players, history | TranslateTransition | Non |
| Sudoku | 5 | Moyen-Difficile | grids, scores | CSS | Non |
| 2048 | 6 | Difficile | scores | ScaleTransition | Non |
| Snake | 6 | Difficile | players, scores | Canvas | AnimationTimer |
| Flappy Bird | 7 | Difficile | players, scores | Canvas | AnimationTimer |
| PacMan | 7 | Difficile | players, scores | Canvas | AnimationTimer |

---
