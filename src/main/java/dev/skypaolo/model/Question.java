package dev.skypaolo.model;

public class Question {

    private final int id; // identifiant pour la base de données
    private final String text; // texte de la question
    private final boolean answer; // réponse correcte
    private final String category; // catégorie de la question

    // constructeur qui crée une question avec ces champs
    public Question(int id, String text, boolean answer, String category) {
        this.id = id;
        this.text = text;
        this.answer = answer;
        this.category = category;
    }

    // Getter du champ id.
    public int getId() {
        return id;
    }

    // Getter du champ text.
    public String getText() {
        return text;
    }

    // Guetter du champs answer.
    public boolean isAnswer() {
        return answer;
    }

    // Guetter du champ category.
    public String getCategory() {
        return category;
    }
}
