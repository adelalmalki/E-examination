package project_2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class AddQuestionFrame extends JFrame {
    private TeacherDashboard dashboard;
    private Exam exam;
    private JTextField questionTextField;
    private JTextField option1Field;
    private JTextField option2Field;
    private JTextField option3Field;
    private JTextField option4Field;
    private JComboBox<String> correctAnswerComboBox;

    public AddQuestionFrame(TeacherDashboard dashboard, Exam exam, JFrame parentFrame) {
        super("Add Question");
        this.dashboard = dashboard;
        this.exam = exam;

        setLayout(new GridLayout(7, 1));
        setSize(400, 300);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        questionTextField = new JTextField(20);
        add(new JLabel("Enter Question:"));
        add(questionTextField);

        option1Field = new JTextField(20);
        option2Field = new JTextField(20);
        option3Field = new JTextField(20);
        option4Field = new JTextField(20);

        add(new JLabel("Option 1:"));
        add(option1Field);
        add(new JLabel("Option 2:"));
        add(option2Field);
        add(new JLabel("Option 3:"));
        add(option3Field);
        add(new JLabel("Option 4:"));
        add(option4Field);

        correctAnswerComboBox = new JComboBox<>(new String[]{
                "Option 1", "Option 2", "Option 3", "Option 4"
        });
        add(new JLabel("Select Correct Answer:"));
        add(correctAnswerComboBox);

        JButton saveButton = new JButton("Save Question");
        add(saveButton);

        saveButton.addActionListener(e -> saveQuestion());
        setVisible(true);
    }

    private void saveQuestion() {
        String questionText = questionTextField.getText().trim();
        ArrayList<String> options = new ArrayList<>();
        options.add(option1Field.getText().trim());
        options.add(option2Field.getText().trim());
        options.add(option3Field.getText().trim());
        options.add(option4Field.getText().trim());

        String correctAnswer = (String) correctAnswerComboBox.getSelectedItem();

        if (!questionText.isEmpty() && options.size() == 4 && correctAnswer != null) {
            try {
                String insertQuestionQuery = "INSERT INTO questions (exam_id, question_text, option1, option2, option3, option4, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = dashboard.getDatabaseConnection().prepareStatement(insertQuestionQuery);
                ps.setInt(1, exam.getId());
                ps.setString(2, questionText);
                ps.setString(3, options.get(0));
                ps.setString(4, options.get(1));
                ps.setString(5, options.get(2));
                ps.setString(6, options.get(3));
                ps.setString(7, correctAnswer);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Question saved successfully!");
                exam.addQuestion(new Question(questionText, options, correctAnswer));
                dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving question.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
        }
    }
}

