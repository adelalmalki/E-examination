package project_2;

import java.util.ArrayList;

public class Question {
    private String questionText;
    private ArrayList<String> options;
    private String correctAnswer;

    public Question(String questionText, ArrayList<String> options, String correctAnswer) {
        if (options.size() != 4) {
            throw new IllegalArgumentException("There must be exactly 4 options.");
        }
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String displayQuestionWithOptions() {
        StringBuilder display = new StringBuilder(questionText + "\n");
        for (int i = 0; i < options.size(); i++) {
            display.append((i + 1)).append(". ").append(options.get(i)).append("\n");
        }
        display.append("Correct Answer: ").append(correctAnswer);
        return display.toString();
    }
}
