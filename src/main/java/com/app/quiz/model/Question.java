package com.app.quiz.model;

import java.util.ArrayList;

public class Question {

    private int id;
    private String questionText;
    private ArrayList<String> options = new ArrayList<>();
    private String correctAnswer;

    public Question() {
        // Default constructor
    }
    
    public Question(int id, String questionText, ArrayList<String> options, String correctAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("Question ID: ").append(id).append("\n");
        sb.append("Question: ").append(questionText).append("\n");
        sb.append("Options:\n");

        for (int i = 0; i < options.size(); i++) {
            sb.append((i + 1)).append(". ").append(options.get(i)).append("\n");
        }

        return sb.toString();

    }

}
