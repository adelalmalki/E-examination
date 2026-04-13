package project_2;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:test.db";

// Getting the connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Create tables in the database
    public static void createTables() {
        String createExamTable = """
                CREATE TABLE IF NOT EXISTS exams (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL
                )""";

        String createTeachersTable = """
            CREATE TABLE IF NOT EXISTS teachers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            )""";
        
        String createQuestionTable = """
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    exam_id INTEGER,
                    question_text TEXT NOT NULL,
                    option1 TEXT NOT NULL,
                    option2 TEXT NOT NULL,
                    option3 TEXT NOT NULL,
                    option4 TEXT NOT NULL,
                    correct_answer TEXT NOT NULL,
                    FOREIGN KEY(exam_id) REFERENCES exams(id)
                )""";

        String createGradeTable = """
                CREATE TABLE IF NOT EXISTS grades (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    exam_id INTEGER,
                    student_name TEXT NOT NULL,
                    grade INTEGER NOT NULL,
                    FOREIGN KEY(exam_id) REFERENCES exams(id)
                )""";
        
        String createStudentAnswersTable = """
        CREATE TABLE IF NOT EXISTS student_answers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            exam_id INTEGER,
            question_id INTEGER,
            selected_answer TEXT NOT NULL,
            FOREIGN KEY(student_id) REFERENCES students(id),  -- ربط student_id بـ students
            FOREIGN KEY(exam_id) REFERENCES exams(id),
            FOREIGN KEY(question_id) REFERENCES questions(id)
        )""";
        
        String createStudentTable = """
            CREATE TABLE IF NOT EXISTS student1 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            )""";
        
         String createStudentsTable = """
            CREATE TABLE IF NOT EXISTS students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            )
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createExamTable);           
            stmt.execute(createQuestionTable);      
            stmt.execute(createGradeTable);          
            stmt.execute(createStudentAnswersTable); 
            stmt.executeUpdate(createStudentsTable); //for student table
            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveStudent(String name, String username, String password) {
        String query = """
            INSERT INTO student1 (name, username, password)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);  
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();
            System.out.println("Student data saved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // Retrieve the list of exams
    public static ArrayList<String> getExams() {
        ArrayList<String> exams = new ArrayList<>();
        String query = "SELECT title FROM exams";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                exams.add(rs.getString("title")); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exams;
    }

    public static int getExamIdByTitle(String examTitle) {
        String query = "SELECT id FROM exams WHERE title = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, examTitle); 
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; 
        }
    }

    // Method to insert the student's answer into the database
    public static void saveStudentAnswer(String studentName, int examId, int questionId, String selectedAnswer) {
        String query = """
        INSERT INTO student_answers (student_name, exam_id, question_id, selected_answer)
        VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, studentName);
            ps.setInt(2, examId);
            ps.setInt(3, questionId);
            ps.setString(4, selectedAnswer);
            ps.executeUpdate();
            System.out.println("Answer saved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
