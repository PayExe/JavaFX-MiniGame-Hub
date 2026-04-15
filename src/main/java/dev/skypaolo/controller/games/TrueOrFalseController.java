package dev.skypaolo.controller.games;

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

public class TrueOrFalseController {

    private final QuestionService questionService = new QuestionService();

    private List<Question> questionQueue;

    private int questionIndex = 0;

    private int score = 0;

    private final int maxQuestions = 10;

    private final ToggleGroup answerSelectionGroup = new ToggleGroup();

    private boolean gameOver = false;

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

    @FXML
    public void initialize() {
        startNewGame();
    }

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

        trueToggle.setToggleGroup(answerSelectionGroup);
        falseToggle.setToggleGroup(answerSelectionGroup);
        trueToggle.setSelected(true);

        updateQuestionCounter();
        showCurrentQuestion();
        setButtonsEnabled(true);
    }

    private void showCurrentQuestion() {
        if (questionIndex < questionQueue.size()) {
            Question currentQuestion = questionQueue.get(questionIndex);
            questionLabel.setText(currentQuestion.getText());
            updateQuestionCounter();
        } else {
            endGame();
        }
    }

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

        FadeTransition fade = new FadeTransition(
            Duration.millis(200),
            feedbackLabel
        );
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

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

    private void setButtonsEnabled(boolean enabled) {
        trueButton.setDisable(!enabled);
        falseButton.setDisable(!enabled);
        nextGameButton.setDisable(!enabled);
    }

    private void updateQuestionCounter() {
        questionIndexLabel.setText(
            (Math.min(questionIndex + 1, questionQueue.size())) +
                "/" +
                questionQueue.size()
        );
    }

    @FXML
    public void handleTrue(ActionEvent event) {
        checkAnswer(true);
    }

    @FXML
    public void handleFalse(ActionEvent event) {
        checkAnswer(false);
    }

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
