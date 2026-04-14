package dev.skypaolo.controller.games;

// j'importe tout ce dont j'ai besoin

import dev.skypaolo.model.Question;
import dev.skypaolo.service.QuestionService;
import java.io.IOException;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.Duration;

// Contrôleur qui gèrele jeu.
// Affiche les questions
// Vérifire les réponses
// Met à jour le score
// Gère l'ajout des questions.s

public class TrueOrFalseController {

    // Ici je demande au service les questions
    private final QuestionService questionService = new QuestionService();

    // Liste des questions dans l'ordre
    private List<Question> questionQueue;

    //Index de la question en cours
    private int questionIndex = 0;

    // Score du joueur
    private int score = 0;

    // Le nombre de questions max par partie.
    private final int maxQuestions = 10;

    // Groupe de boutons pour forcer un seul choix entre vrai et faux.
    private final ToggleGroup answerSelectionGroup = new ToggleGroup();

    // Bool de l'état de la partie.
    private boolean gameOver = false;

    // Les éléments FXML.
    @FXML
    private Label scoreLabel;

    @FXML
    private Label questionLabel;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Label questionIndexLabel;

    @FXML
    private Label gameStateLabel;

    @FXML
    private Button trueButton;

    @FXML
    private Button falseButton;

    @FXML
    private Button nextGameButton;

    @FXML
    private Button backButton;

    @FXML
    private Button addQuestionButton;

    @FXML
    private TextField newQuestionField;

    @FXML
    private TextField categoryField;

    @FXML
    private ToggleGroup answerToggleGroup;

    @FXML
    private ToggleButton trueToggle;

    @FXML
    private ToggleButton falseToggle;

    // On initialise le jeu, c'est appelé automatiquement.
    @FXML
    public void initialize() {
        startNewGame();
    }

    // Lance une nouvelle partie.
    private void startNewGame() {
        questionQueue = questionService.getShuffledQuestions();
        if (questionQueue.size() > maxQuestions) {
            questionQueue = questionQueue.subList(0, maxQuestions);
        }

        questionIndex = 0;
        score = 0;
        gameOver = false;

        scoreLabel.setText(String.valueOf(score));
        gameStateLabel.setText("En cours");
        feedbackLabel.setText("");
        feedbackLabel
            .getStyleClass()
            .removeAll("feedback-win", "feedback-loss");
        feedbackLabel.setOpacity(1.0);

        // Associe les deux boutons à un seul groupe pour n'autoriser qu'un seul choix.
        trueToggle.setToggleGroup(answerSelectionGroup);
        falseToggle.setToggleGroup(answerSelectionGroup);
        trueToggle.setSelected(true);

        updateQuestionCounter();
        showCurrentQuestion();
        setButtonsEnabled(true);
    }

    // Affiche la question actuelle.
    private void showCurrentQuestion() {
        if (questionIndex < questionQueue.size()) {
            Question currentQuestion = questionQueue.get(questionIndex);
            questionLabel.setText(currentQuestion.getText());
            updateQuestionCounter();
        } else {
            endGame();
        }
    }

    // Vérifie la réponse du joueur
    private void checkAnswer(boolean expectedAnswer) {
        if (gameOver || questionIndex >= questionQueue.size()) {
            return;
        }

        Question currentQuestion = questionQueue.get(questionIndex);
        boolean playerAnswer = expectedAnswer;
        boolean correct = (playerAnswer == currentQuestion.isAnswer());

        if (correct) {
            score++;
            feedbackLabel.setText("Bonne réponse !");
            feedbackLabel.getStyleClass().removeAll("feedback-loss");
            if (!feedbackLabel.getStyleClass().contains("feedback-win")) {
                feedbackLabel.getStyleClass().add("feedback-win");
            }
        } else {
            feedbackLabel.setText("Mauvaise réponse !");
            feedbackLabel.getStyleClass().removeAll("feedback-win");
            if (!feedbackLabel.getStyleClass().contains("feedback-loss")) {
                feedbackLabel.getStyleClass().add("feedback-loss");
            }
        }

        scoreLabel.setText(String.valueOf(score));

        // Fondu
        FadeTransition fade = new FadeTransition(
            Duration.millis(200),
            feedbackLabel
        );
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        // Pause le temps que le joueur lise le message avant de passer à la question suivante.
        PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
        pause.setOnFinished(e -> {
            questionIndex++;
            if (questionIndex >= questionQueue.size()) {
                endGame();
            } else {
                feedbackLabel.setText("");
                feedbackLabel
                    .getStyleClass()
                    .removeAll("feedback-win", "feedback-loss");
                feedbackLabel.setOpacity(0.0);
                showCurrentQuestion();
            }
        });
        pause.play();
    }

    // Ca mets fin à la partie (désactive les boutons et affiche le score)
    private void endGame() {
        gameOver = true;
        gameStateLabel.setText("Terminée");
        questionLabel.setText(
            "Partie terminée ! Score final : " +
                score +
                "/" +
                questionQueue.size()
        );
        feedbackLabel.setText("Bravo, tu as terminé la partie !");
        feedbackLabel.setOpacity(1.0);
        setButtonsEnabled(false);
    }

    // Active ou désactive les boutons du jeu.
    private void setButtonsEnabled(boolean enabled) {
        trueButton.setDisable(!enabled);
        falseButton.setDisable(!enabled);
        nextGameButton.setDisable(!enabled);
    }

    // Mets à jour l'affichage du compteur de questions.
    private void updateQuestionCounter() {
        questionIndexLabel.setText(
            (Math.min(questionIndex + 1, questionQueue.size())) +
                "/" +
                questionQueue.size()
        );
    }

    // Appelé quand le joueur clique sur vrai.
    @FXML
    public void handleTrue(ActionEvent event) {
        checkAnswer(true);
    }

    // Appelé quand le joueur clique sur faux
    @FXML
    public void handleFalse(ActionEvent event) {
        checkAnswer(false);
    }

    // Bouton pour passer à la question suivante.
    @FXML
    public void handleNextQuestion(ActionEvent event) {
        if (!gameOver) {
            questionIndex++;
            if (questionIndex >= questionQueue.size()) {
                endGame();
            } else {
                feedbackLabel.setText("");
                feedbackLabel
                    .getStyleClass()
                    .removeAll("feedback-win", "feedback-loss");
                feedbackLabel.setOpacity(0.0);
                showCurrentQuestion();
            }
        }
    }

    // Permet d'ajouter une nouvelle question.
    @FXML
    public void handleAddQuestion(ActionEvent event) {
        String text = newQuestionField.getText().trim();
        String category = categoryField.getText().trim();

        if (text.isEmpty()) {
            feedbackLabel.setText("La question ne peut pas être vide.");
            return;
        }

        if (category.isEmpty()) {
            category = "général";
        }

        boolean answer = trueToggle.isSelected();

        int newId = questionService.getQuestionCount() + 1;
        Question newQuestion = new Question(newId, text, answer, category);
        questionService.addQuestion(newQuestion);

        newQuestionField.clear();
        categoryField.clear();
        trueToggle.setSelected(true);
        falseToggle.setSelected(false);

        feedbackLabel.setText("Question ajoutée avec succès !");
    }

    // Retour au hub.
    @FXML
    public void handleBackToHub(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MinigameHubView.fxml")
            );
            Parent hubView = loader.load();

            Stage currentStage = (Stage) (
                (Node) event.getSource()
            ).getScene().getWindow();
            Scene scene = new Scene(hubView, 800, 600);
            scene
                .getStylesheets()
                .add(
                    getClass()
                        .getResource("/css/vault-tec-theme.css")
                        .toExternalForm()
                );

            currentStage.setScene(scene);
            currentStage.setTitle("Vault-Tec Mini Game Hub");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
