package project_2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private Connection connection;
    private JButton loginButton;
    private JButton adminPageButton;
    private AdminPage adminPage; 


    public LoginPage(Connection connection) {
        this.connection = connection;  // The connection to the database is passed here.

        setTitle("Login Page");
        setLayout(new BorderLayout());
        setSize(400, 300); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

        // Setting up the frame
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Adding the title "Login"
        JLabel titleLabel = new JLabel("Welcome to the Login Page", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel spacerPanel = new JPanel();
        spacerPanel.setPreferredSize(new Dimension(400, 20)); 
        mainPanel.add(spacerPanel, BorderLayout.CENTER);

        // Setting up the form (texts and buttons)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        loginButton = new JButton("Login");
        adminPageButton = new JButton("Go to Admin Page");

        // Formatting the buttons
        loginButton.setBackground(new Color(34, 167, 240));
        loginButton.setForeground(Color.WHITE);
        adminPageButton.setBackground(new Color(192, 57, 43));
        adminPageButton.setForeground(Color.WHITE);

        // Adding events to the buttons
        loginButton.addActionListener(e -> {
            try {
                login();
            } catch (SQLException ex) {
                Logger.getLogger(LoginPage.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        adminPageButton.addActionListener(e -> openAdminPage());

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        // Setting up the button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(adminPageButton);

        // Adding components to the frame
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Adding components to the frame
        add(mainPanel);

        setLocationRelativeTo(null); // Centering the application window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Keep the window open when closed

    }

    // Method for logging in and verifying credentials
    private void login() throws SQLException {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Verifying the credentials and retrieving the user type
        String userType = checkCredentials(username, password);
        if (userType != null) {
            JOptionPane.showMessageDialog(this, "Login successful!");

            if (userType.equals("teacher")) {
               new TeacherDashboard(connection).display();
                

            } else if (userType.equals("student")) {
               new StudentDashboard(connection).display();  // Show the window
                }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
            
    }

    // Method to check the credentials in the database
   private String checkCredentials(String username, String password) {
        // Query to verify teacher
        String queryTeacher = "SELECT * FROM teachers WHERE username = ? AND password = ?";
        // Query to verify student
        String queryStudent = "SELECT * FROM student1 WHERE username = ? AND password = ?";  

        try (PreparedStatement psTeacher = connection.prepareStatement(queryTeacher)) {
            psTeacher.setString(1, username);
            psTeacher.setString(2, password);
            ResultSet rsTeacher = psTeacher.executeQuery();

            if (rsTeacher.next()) {
                return "teacher"; // Found in teachers table
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement psStudent = connection.prepareStatement(queryStudent)) {
            psStudent.setString(1, username);
            psStudent.setString(2, password);
            ResultSet rsStudent = psStudent.executeQuery();

            if (rsStudent.next()) {
                return "student"; // Found in student1 table
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // No match found
    }

    // Method to open the admin page
    private void openAdminPage() {
        // When the "Admin Login" button is pressed, the admin page will open
        if (adminPage == null) {
            adminPage = new AdminPage(); 
            adminPage.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
            adminPage.setSize(600, 400); 
            adminPage.setLocationRelativeTo(this); 
        }
        adminPage.setVisible(true); // Open the admin page
    }

    public static void main(String[] args) {
        // Connecting to the database using DatabaseHelper
        try {
            Connection connection = DatabaseHelper.getConnection();
            LoginPage loginPage = new LoginPage(connection); // Create the login page window
            loginPage.setVisible(true); // Display the window
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage());
        }
    }
}
