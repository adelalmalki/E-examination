package project_2;

import java.util.ArrayList;

public class Exam {
    private String title;
    private ArrayList<Question> questions;
    private int id;  

    // Constructor with id addition
    public Exam(String title, ArrayList<Question> questions, int id) {
        this.title = title;
        this.questions = questions;
        this.id = id;  
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public int getId() {
        return id;  
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    // Display exam details: title + questions with options
    public String displayExamDetails() {
        StringBuilder details = new StringBuilder("Exam: " + title + "\n\n");

        // Add each question with its options
        for (Question question : questions) {
            details.append(question.displayQuestionWithOptions()).append("\n\n");
        }

        return details.toString();  // Return the full text
    }
}

