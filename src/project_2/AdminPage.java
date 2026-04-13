package project_2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminPage extends JFrame {

    // Database connection details
    private static final String DB_URL = "jdbc:sqlite:test.db";

    // Fields for teacher and student sections
    private JTextField teacherUsernameField;
    private JPasswordField teacherPasswordField;
    private JTextField studentUsernameField;
    private JPasswordField studentPasswordField;

    public AdminPage() {
        setTitle("Admin Page");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create split pane to divide the screen into two sections
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTeacherPanel(), createStudentPanel());
        splitPane.setDividerLocation(300); // Set divider location
        add(splitPane);

        // Add the page title in large font
        JLabel pageTitle = new JLabel("Admin Page", JLabel.CENTER);
        pageTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(pageTitle, BorderLayout.NORTH);

        // Add the save button at the bottom
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));

        // Set preferred size for the save button
        saveButton.setPreferredSize(new Dimension(100, 30)); // Width: 100px, Height: 30px

        saveButton.addActionListener(e -> saveAndReset());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/project_2/images/icon.jpg"));
        setIconImage(icon.getImage());

        setLocationRelativeTo(null); // Center the window on the screen
    }

    private JPanel createTeacherPanel() {
        JPanel teacherPanel = new JPanel();
        teacherPanel.setLayout(new BoxLayout(teacherPanel, BoxLayout.Y_AXIS));

        // Teacher section header
        JLabel teacherName = new JLabel("Teacher Section");
        teacherName.setFont(new Font("Arial", Font.BOLD, 18));
        teacherPanel.add(teacherName);

        teacherPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Label for username
        JLabel usernameLabel = new JLabel("Enter Teacher Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        teacherPanel.add(usernameLabel);

        // Username field (empty)
        teacherUsernameField = new JTextField();
        teacherPanel.add(teacherUsernameField);

        // Label for password
        JLabel passwordLabel = new JLabel("Enter Teacher Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        teacherPanel.add(passwordLabel);

        // Password field (empty)
        teacherPasswordField = new JPasswordField();
        teacherPanel.add(teacherPasswordField);

        return teacherPanel;
    }

    private JPanel createStudentPanel() {
        JPanel studentPanel = new JPanel();
        studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));

        // Student section header
        JLabel studentName = new JLabel("Student Section");
        studentName.setFont(new Font("Arial", Font.BOLD, 18));
        studentPanel.add(studentName);

        studentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Label for username
        JLabel usernameLabel = new JLabel("Enter Student Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        studentPanel.add(usernameLabel);

        // Username field (empty)
        studentUsernameField = new JTextField();
        studentPanel.add(studentUsernameField);

        // Label for password
        JLabel passwordLabel = new JLabel("Enter Student Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        studentPanel.add(passwordLabel);

        // Password field (empty)
        studentPasswordField = new JPasswordField();
        studentPanel.add(studentPasswordField);

        return studentPanel;
    }

    private void saveAndReset() {
        String teacherUsername = teacherUsernameField.getText();
        String teacherPassword = new String(teacherPasswordField.getPassword());
        String studentUsername = studentUsernameField.getText();
        String studentPassword = new String(studentPasswordField.getPassword());

        boolean isTeacherSaved = false;
        boolean isStudentSaved = false;

        if (!teacherUsername.isEmpty() || !teacherPassword.isEmpty()) {
            saveData("teachers", teacherUsername, teacherPassword);  // Passing only the username and password

            isTeacherSaved = true;
        }

        if (!studentUsername.isEmpty() || !studentPassword.isEmpty()) {
            saveData("student1", studentUsername, studentPassword);  // Passing only the username and password

            isStudentSaved = true;
        }

        if (isTeacherSaved || isStudentSaved) {
            JOptionPane.showMessageDialog(this, "Data saved successfully!");
            resetFields(); // Clear the fields
        } else {
            JOptionPane.showMessageDialog(this, "Please fill in at least one field in either section.");
        }
    }

    // Method to save data to the SQLite database
    private void saveData(String tableName, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return; // Skip saving if any field is empty
        }

        String sql = "INSERT INTO " + tableName + " (username, password) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
        }
    }

    // Method to clear all fields
    private void resetFields() {
        teacherUsernameField.setText("");
        teacherPasswordField.setText("");
        studentUsernameField.setText("");
        studentPasswordField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminPage().setVisible(true);
        });
    }
}

