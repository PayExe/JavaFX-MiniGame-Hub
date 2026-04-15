package dev.skypaolo.model;

public class Question {

    private final int id;
    private final String text;
    private final boolean answer;
    private final String category;

    public Question(int id, String text, boolean answer, String category) {
        this.id = id;
        this.text = text;
        this.answer = answer;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean isAnswer() {
        return answer;
    }

    public String getCategory() {
        return category;
    }
}
