package project_2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class TeacherDashboard extends JFrame {
    private ArrayList<Exam> exams = new ArrayList<>();
    private DefaultListModel<String> examListModel = new DefaultListModel<>();
    private Connection conn;

    // Constructor
    public TeacherDashboard(Connection conn) {
        this.conn = conn;
    }

    // Getter for exams
    public ArrayList<Exam> getQuestions() {
        return exams;
    }

    // Getter for exam list model
    public DefaultListModel<String> getExamListModel() {
        return examListModel;
    }

    // Database connection getter
    public Connection getDatabaseConnection() {
        return conn;
    }

    // Display Teacher Dashboard
    public void display() {
        JFrame frame = new JFrame("Teacher Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // Title
        JLabel title = new JLabel("Teacher Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        frame.add(title, BorderLayout.NORTH);

        // Left panel with buttons
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JButton createExamButton = new JButton("Create Exam");
        JButton gradeExamButton = new JButton("Grade Exams");
        JButton showExamButton = new JButton("Show Exams");

        Dimension buttonSize = new Dimension(120, 30);
        createExamButton.setMaximumSize(buttonSize);
        gradeExamButton.setMaximumSize(buttonSize);
        showExamButton.setMaximumSize(buttonSize);

        leftPanel.add(createExamButton);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(gradeExamButton);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(showExamButton);
        leftPanel.add(Box.createVerticalGlue());

        frame.add(leftPanel, BorderLayout.WEST);

        // Center panel with exam list
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JList<String> examList = new JList<>(examListModel);
        JScrollPane examListScrollPane = new JScrollPane(examList);
        examListScrollPane.setBorder(BorderFactory.createTitledBorder("Exams List"));
        examListScrollPane.setPreferredSize(new Dimension(250, 100));
        centerPanel.add(examListScrollPane);

        frame.add(centerPanel, BorderLayout.CENTER);

        // Button actions
        createExamButton.addActionListener(e -> showCreateExamFrame());

        showExamButton.addActionListener(e -> {
            loadExams();
            examList.setModel(examListModel);

            examList.addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    String selectedExamTitle = examList.getSelectedValue();
                    if (selectedExamTitle != null) {
                        Exam selectedExam = getExamByTitle(selectedExamTitle);
                        showExamQuestionsWithGrades(selectedExam);
                    }
                }
            });
        });

        gradeExamButton.addActionListener(e -> showSubmittedExamsWithAnswers());

        frame.setVisible(true);
    }

    // Load exams from database
    private void loadExams() {
        examListModel.clear();
        try {
            String query = "SELECT * FROM exams";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String examTitle = rs.getString("title");
                examListModel.addElement(examTitle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch exam by title
    private Exam getExamByTitle(String title) {
        try {
            String query = "SELECT * FROM exams WHERE title = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ArrayList<Question> questions = new ArrayList<>();
                int examId = rs.getInt("id");

                String questionQuery = "SELECT * FROM questions WHERE exam_id = ?";
                PreparedStatement psQuestion = conn.prepareStatement(questionQuery);
                psQuestion.setInt(1, examId);
                ResultSet questionRs = psQuestion.executeQuery();

                while (questionRs.next()) {
                    String questionText = questionRs.getString("question_text");
                    ArrayList<String> options = new ArrayList<>();
                    options.add(questionRs.getString("option1"));
                    options.add(questionRs.getString("option2"));
                    options.add(questionRs.getString("option3"));
                    options.add(questionRs.getString("option4"));
                    String correctAnswer = questionRs.getString("correct_answer");

                    questions.add(new Question(questionText, options, correctAnswer));
                }

                return new Exam(title, questions, examId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Display exam questions and grades
    private void showExamQuestionsWithGrades(Exam exam) {
        JFrame examFrame = new JFrame("Exam Questions");
        examFrame.setSize(800, 400);
        examFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        examFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (Question question : exam.getQuestions()) {
            JPanel questionPanel = new JPanel();
            questionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            JLabel questionLabel = new JLabel(question.getQuestionText());
            questionPanel.add(questionLabel);

            ButtonGroup group = new ButtonGroup();
            for (String option : question.getOptions()) {
                JRadioButton optionButton = new JRadioButton(option);
                group.add(optionButton);
                questionPanel.add(optionButton);
            }

            panel.add(questionPanel);
        }

        examFrame.add(panel);
        examFrame.setVisible(true);
    }

    // Display submitted exams with answers
    private void showSubmittedExamsWithAnswers() {
    JFrame examFrame = new JFrame("Submitted Exams and Answers");
    examFrame.setSize(800, 600);
    examFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    examFrame.setLocationRelativeTo(null);

    // Main panel
    JPanel mainPanel = new JPanel(new BorderLayout());
    JPanel topPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10); // Add padding
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Dropdown and load button
    JLabel examLabel = new JLabel("Select an exam:");
    JComboBox<String> examDropdown = new JComboBox<>();
    examDropdown.setPreferredSize(new Dimension(150, 25)); // Smaller size for combo box

    String examQuery = """
        SELECT DISTINCT e.title 
        FROM exams e
        JOIN student_answers sa ON e.id = sa.exam_id
    """;

    try (PreparedStatement ps = conn.prepareStatement(examQuery); ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            examDropdown.addItem(rs.getString("title"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error loading exams!");
    }

    gbc.gridx = 0;
    gbc.gridy = 0;
    topPanel.add(examLabel, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    topPanel.add(examDropdown, gbc);

    JButton loadButton = new JButton("Load Exam");
    loadButton.setPreferredSize(new Dimension(100, 30)); // Adjust button size
    gbc.gridx = 2;
    gbc.gridy = 0;
    topPanel.add(loadButton, gbc);

    // Result panel for displaying exam data
    JPanel resultPanel = new JPanel();
    resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
    JScrollPane resultScrollPane = new JScrollPane(resultPanel);

    // Add components to main panel
    mainPanel.add(topPanel, BorderLayout.NORTH);
    mainPanel.add(resultScrollPane, BorderLayout.CENTER);

    // Load button action
    loadButton.addActionListener(e -> {
        String selectedExam = (String) examDropdown.getSelectedItem();
        if (selectedExam != null) {
            loadExamData(selectedExam, resultPanel); // Pass resultPanel to loadExamData
        }
    });

    examFrame.add(mainPanel);
    examFrame.setVisible(true);
}

    private void loadExamData(String selectedExam, JPanel resultPanel) {
        resultPanel.removeAll(); // Clear previous data

        try {
            String query = """
                SELECT sq.student_name AS student_name, e.title AS exam_title,
                       q.question_text AS question, q.option1, q.option2,
                       q.option3, q.option4, sq.selected_answer AS student_answer
                FROM student_answers sq
                JOIN questions q ON sq.question_id = q.id
                JOIN exams e ON q.exam_id = e.id
                WHERE e.title = ?
                ORDER BY sq.student_name, q.id;
            """;

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, selectedExam);
            ResultSet rs = ps.executeQuery();

            String currentStudent = "";
            JPanel studentPanel = null;

            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                String studentName = rs.getString("student_name");
                String questionText = rs.getString("question");
                String option1 = rs.getString("option1");
                String option2 = rs.getString("option2");
                String option3 = rs.getString("option3");
                String option4 = rs.getString("option4");
                String studentAnswer = rs.getString("student_answer");

                if (!studentName.equals(currentStudent)) {
                    currentStudent = studentName;
                    studentPanel = new JPanel();
                    studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));
                    studentPanel.setBorder(BorderFactory.createTitledBorder("Student: " + studentName));
                    resultPanel.add(studentPanel);
                }

                JPanel questionPanel = new JPanel();
                questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
                questionPanel.add(new JLabel("Question: " + questionText));
                questionPanel.add(new JLabel("Options: " + option1 + ", " + option2 + ", " + option3 + ", " + option4));
                questionPanel.add(new JLabel("Selected Answer: " + studentAnswer));

                if (studentPanel != null) {
                    studentPanel.add(questionPanel);
                }
            }

            if (!hasData) {
                JOptionPane.showMessageDialog(null, "No data found for the selected exam!");
            }

            // Refresh the result panel
            resultPanel.revalidate();
            resultPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading data!");
        }
    }


    // Display create exam frame
    private void showCreateExamFrame() {
    JFrame createExamFrame = new JFrame("Create Exam");
    createExamFrame.setSize(400, 300);
    createExamFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    createExamFrame.setLocationRelativeTo(null);

    // Main panel with BorderLayout
    JPanel mainPanel = new JPanel(new BorderLayout());

    // Add a header label at the top
    JLabel headerLabel = new JLabel("Create Exam Page");
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT); // Align text to the left
    headerLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Set bold font with larger size
    headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding around the label
    mainPanel.add(headerLabel, BorderLayout.NORTH);

    // Form panel for input fields and buttons
    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 10, 10, 10);

    JLabel titleLabel = new JLabel("Title:");
    JTextField titleField = new JTextField(15);

    JButton saveButton = new JButton("Save");
    saveButton.setPreferredSize(new Dimension(80, 30)); // Set a smaller size for the button
    saveButton.addActionListener(e -> {
        String title = titleField.getText();
        if (!title.isEmpty()) {
            int examId = createExam(title);
            if (examId != -1) {
                showAddQuestionFrame(examId);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please enter an exam title!");
        }
    });

    // Add components to the form panel
    gbc.gridx = 0;
    gbc.gridy = 0;
    formPanel.add(titleLabel, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    formPanel.add(titleField, gbc);

    gbc.gridx = 1;
    gbc.gridy = 1;
    formPanel.add(saveButton, gbc);

    // Add the form panel to the center
    mainPanel.add(formPanel, BorderLayout.CENTER);

    createExamFrame.add(mainPanel);
    createExamFrame.setVisible(true);
}


    private void showAddQuestionFrame(int examId) {
    JFrame addQuestionFrame = new JFrame("Add Question");
    addQuestionFrame.setSize(400, 400);
    addQuestionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    addQuestionFrame.setLocationRelativeTo(null);

    // Panel with GridBagLayout for better organization
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 10, 10, 10); // Padding between components

    // Components for adding question
    JLabel questionLabel = new JLabel("Question:");
    JTextField questionField = new JTextField(20);

    JLabel option1Label = new JLabel("Option 1:");
    JTextField option1Field = new JTextField(20);

    JLabel option2Label = new JLabel("Option 2:");
    JTextField option2Field = new JTextField(20);

    JLabel option3Label = new JLabel("Option 3:");
    JTextField option3Field = new JTextField(20);

    JLabel option4Label = new JLabel("Option 4:");
    JTextField option4Field = new JTextField(20);

    JLabel correctAnswerLabel = new JLabel("Correct Answer:");

    // Dropdown menu with fixed options
    JComboBox<String> correctAnswerCombo = new JComboBox<>(new String[]{
        "Option 1", "Option 2", "Option 3", "Option 4"
    });

    // Save button
    JButton saveButton = new JButton("Save Question");

    // Clear fields button
    JButton clearButton = new JButton("Clear Fields");

    // Add components to panel with layout constraints
    gbc.gridx = 0; gbc.gridy = 0; panel.add(questionLabel, gbc);
    gbc.gridx = 1; gbc.gridy = 0; panel.add(questionField, gbc);

    gbc.gridx = 0; gbc.gridy = 1; panel.add(option1Label, gbc);
    gbc.gridx = 1; gbc.gridy = 1; panel.add(option1Field, gbc);

    gbc.gridx = 0; gbc.gridy = 2; panel.add(option2Label, gbc);
    gbc.gridx = 1; gbc.gridy = 2; panel.add(option2Field, gbc);

    gbc.gridx = 0; gbc.gridy = 3; panel.add(option3Label, gbc);
    gbc.gridx = 1; gbc.gridy = 3; panel.add(option3Field, gbc);

    gbc.gridx = 0; gbc.gridy = 4; panel.add(option4Label, gbc);
    gbc.gridx = 1; gbc.gridy = 4; panel.add(option4Field, gbc);

    gbc.gridx = 0; gbc.gridy = 5; panel.add(correctAnswerLabel, gbc);
    gbc.gridx = 1; gbc.gridy = 5; panel.add(correctAnswerCombo, gbc);

    gbc.gridx = 0; gbc.gridy = 6; panel.add(saveButton, gbc);
    gbc.gridx = 1; gbc.gridy = 6; panel.add(clearButton, gbc);

    // Action listeners
    saveButton.addActionListener(e -> {
        String questionText = questionField.getText().trim();
        String option1 = option1Field.getText().trim();
        String option2 = option2Field.getText().trim();
        String option3 = option3Field.getText().trim();
        String option4 = option4Field.getText().trim();
        String correctAnswer = (String) correctAnswerCombo.getSelectedItem();

        if (questionText.isEmpty() || option1.isEmpty() || option2.isEmpty() ||
            option3.isEmpty() || option4.isEmpty() || correctAnswer == null) {
            JOptionPane.showMessageDialog(null, "Please fill all fields and select a correct answer!");
            return;
        }

        // Map the correct answer to its corresponding text
        String correctAnswerText = switch (correctAnswer) {
            case "Option 1" -> option1;
            case "Option 2" -> option2;
            case "Option 3" -> option3;
            case "Option 4" -> option4;
            default -> null;
        };
        saveQuestionToDatabase(examId, questionText, option1, option2, option3, option4, correctAnswer);

        JOptionPane.showMessageDialog(null, "Question saved successfully!");

        // Clear fields
        questionField.setText("");
        option1Field.setText("");
        option2Field.setText("");
        option3Field.setText("");
        option4Field.setText("");
        correctAnswerCombo.setSelectedIndex(0);

    });

    clearButton.addActionListener(e -> {
        questionField.setText("");
        option1Field.setText("");
        option2Field.setText("");
        option3Field.setText("");
        option4Field.setText("");
        correctAnswerCombo.setSelectedIndex(0);

    });

    addQuestionFrame.add(panel);
    addQuestionFrame.setVisible(true);
}

// Save question and answers to database
private void saveQuestionToDatabase(int examId, String questionText, String option1, String option2, String option3, String option4, String correctAnswer) {
    String query = """
        INSERT INTO questions (exam_id, question_text, option1, option2, option3, option4, correct_answer)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    try (Connection conn = DatabaseHelper.getConnection();
         PreparedStatement ps = conn.prepareStatement(query)) {
        ps.setInt(1, examId);
        ps.setString(2, questionText);
        ps.setString(3, option1);
        ps.setString(4, option2);
        ps.setString(5, option3);
        ps.setString(6, option4);
        ps.setString(7, correctAnswer);
        ps.executeUpdate();
        JOptionPane.showMessageDialog(null, "Question saved successfully!");
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error saving question!");
    }
}
    private int createExam(String title) {
        String query = "INSERT INTO exams (title) VALUES (?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int examId = rs.getInt(1); // Return the auto-generated exam ID
                JOptionPane.showMessageDialog(null, "Exam created successfully!");
                return examId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating exam!");
        }
        return -1;
    }
    }
