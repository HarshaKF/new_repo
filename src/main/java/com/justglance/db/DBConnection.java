package com.justglance.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DBConnection {
    private static final int MAX_POOL_SIZE = 10;
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/just_glance_tuition?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";  // WAMP MySQL typically has no password
    
    private static BlockingQueue<Connection> connectionPool;
    private static DBConnection instance;
    
    private DBConnection() {
        try {
            Class.forName(DRIVER_CLASS);
            initializeConnectionPool();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Make sure you have the MySQL connector JAR in your classpath.");
            throw new RuntimeException("Failed to load JDBC driver.", e);
        }
    }
    
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }
    
    private void initializeConnectionPool() {
        connectionPool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            try {
                Connection connection = createNewConnection();
                connectionPool.offer(connection);
            } catch (SQLException e) {
                System.err.println("Error initializing connection pool: " + e.getMessage());
            }
        }
    }
    
    private Connection createNewConnection() throws SQLException {
        try {
            Properties props = new Properties();
            props.setProperty("user", USERNAME);
            props.setProperty("password", PASSWORD);
            props.setProperty("autoReconnect", "true");
            props.setProperty("useSSL", "false");
            props.setProperty("allowPublicKeyRetrieval", "true");
            
            Connection conn = DriverManager.getConnection(URL, props);
            System.out.println("Successfully connected to the database!");
            return conn;
        } catch (SQLException e) {
            System.err.println("Failed to connect to database. Error: " + e.getMessage());
            System.err.println("Please ensure:");
            System.err.println("1. MySQL server is running");
            System.err.println("2. Username and password are correct");
            System.err.println("3. Database 'just_glance_tuition' exists");
            throw e;
        }
    }
    
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = connectionPool.take();
            if (!isConnectionValid(connection)) {
                connection = createNewConnection();
            }
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }
    
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed() && isConnectionValid(connection)) {
                    connectionPool.offer(connection);
                } else {
                    createNewConnection(); // Replace invalid connection
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
            }
        }
    }
    
    private boolean isConnectionValid(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void shutdown() {
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    public void createTables() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            
            // Create Students table
            String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    phone VARCHAR(20),
                    address TEXT,
                    date_of_birth DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.executeUpdate(createStudentsTable);
            System.out.println("Students table created successfully!");
            
            // Create Courses table
            String createCoursesTable = """
                CREATE TABLE IF NOT EXISTS courses (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    description TEXT,
                    duration VARCHAR(50),
                    fee DECIMAL(10,2),
                    max_students INT,
                    schedule TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.executeUpdate(createCoursesTable);
            System.out.println("Courses table created successfully!");
            
            // Create Faculty table
            String createFacultyTable = """
                CREATE TABLE IF NOT EXISTS faculty (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    phone VARCHAR(20),
                    qualification TEXT,
                    specialization VARCHAR(100),
                    experience_years INT,
                    joining_date DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.executeUpdate(createFacultyTable);
            System.out.println("Faculty table created successfully!");
            
            // Create Enrollments table (junction table for students and courses)
            String createEnrollmentsTable = """
                CREATE TABLE IF NOT EXISTS enrollments (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    student_id INT,
                    course_id INT,
                    enrollment_date DATE,
                    fee_paid DECIMAL(10,2),
                    status ENUM('active', 'completed', 'dropped') DEFAULT 'active',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES students(id),
                    FOREIGN KEY (course_id) REFERENCES courses(id)
                )
            """;
            stmt.executeUpdate(createEnrollmentsTable);
            System.out.println("Enrollments table created successfully!");
            
            // Create Attendance table
            String createAttendanceTable = """
                CREATE TABLE IF NOT EXISTS attendance (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    student_id INT,
                    course_id INT,
                    date DATE,
                    status ENUM('present', 'absent', 'late') DEFAULT 'present',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES students(id),
                    FOREIGN KEY (course_id) REFERENCES courses(id)
                )
            """;
            stmt.executeUpdate(createAttendanceTable);
            System.out.println("Attendance table created successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) releaseConnection(conn);
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    public void insertSampleData() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);  // Start transaction
            
            // Insert sample faculty data
            String insertFaculty = "INSERT INTO faculty (name, email, phone, qualification, specialization, experience_years, joining_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertFaculty);
            
            // Faculty 1
            pstmt.setString(1, "Dr. Rajesh Kumar");
            pstmt.setString(2, "rajesh.kumar@justglance.com");
            pstmt.setString(3, "9876543210");
            pstmt.setString(4, "PhD in Mathematics");
            pstmt.setString(5, "Advanced Mathematics");
            pstmt.setInt(6, 15);
            pstmt.setDate(7, java.sql.Date.valueOf("2020-01-15"));
            pstmt.executeUpdate();
            
            // Faculty 2
            pstmt.setString(1, "Prof. Priya Singh");
            pstmt.setString(2, "priya.singh@justglance.com");
            pstmt.setString(3, "9876543211");
            pstmt.setString(4, "MSc in Physics");
            pstmt.setString(5, "Physics");
            pstmt.setInt(6, 10);
            pstmt.setDate(7, java.sql.Date.valueOf("2021-03-20"));
            pstmt.executeUpdate();
            
            System.out.println("Sample faculty data inserted successfully!");
            
            // Insert sample courses
            String insertCourse = "INSERT INTO courses (name, description, duration, fee, max_students, schedule) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertCourse);
            
            // Course 1
            pstmt.setString(1, "IIT-JEE Mathematics");
            pstmt.setString(2, "Comprehensive mathematics course for IIT-JEE preparation");
            pstmt.setString(3, "6 months");
            pstmt.setBigDecimal(4, new java.math.BigDecimal("15000.00"));
            pstmt.setInt(5, 30);
            pstmt.setString(6, "Mon, Wed, Fri - 4:00 PM to 6:00 PM");
            pstmt.executeUpdate();
            
            // Course 2
            pstmt.setString(1, "NEET Physics");
            pstmt.setString(2, "Complete physics preparation for NEET examination");
            pstmt.setString(3, "6 months");
            pstmt.setBigDecimal(4, new java.math.BigDecimal("12000.00"));
            pstmt.setInt(5, 25);
            pstmt.setString(6, "Tue, Thu, Sat - 3:00 PM to 5:00 PM");
            pstmt.executeUpdate();
            
            System.out.println("Sample courses data inserted successfully!");
            
            // Insert sample students
            String insertStudent = "INSERT INTO students (name, email, phone, address, date_of_birth) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertStudent);
            
            // Student 1
            pstmt.setString(1, "Rahul Sharma");
            pstmt.setString(2, "rahul.sharma@email.com");
            pstmt.setString(3, "9876543212");
            pstmt.setString(4, "123 Main Street, Mumbai");
            pstmt.setDate(5, java.sql.Date.valueOf("2005-05-15"));
            pstmt.executeUpdate();
            
            // Student 2
            pstmt.setString(1, "Priya Patel");
            pstmt.setString(2, "priya.patel@email.com");
            pstmt.setString(3, "9876543213");
            pstmt.setString(4, "456 Park Avenue, Delhi");
            pstmt.setDate(5, java.sql.Date.valueOf("2004-08-20"));
            pstmt.executeUpdate();
            
            System.out.println("Sample students data inserted successfully!");
            
            // Insert sample enrollments
            String insertEnrollment = "INSERT INTO enrollments (student_id, course_id, enrollment_date, fee_paid, status) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertEnrollment);
            
            // Enrollment 1
            pstmt.setInt(1, 1);  // Rahul Sharma
            pstmt.setInt(2, 1);  // IIT-JEE Mathematics
            pstmt.setDate(3, java.sql.Date.valueOf("2025-01-15"));
            pstmt.setBigDecimal(4, new java.math.BigDecimal("15000.00"));
            pstmt.setString(5, "active");
            pstmt.executeUpdate();
            
            // Enrollment 2
            pstmt.setInt(1, 2);  // Priya Patel
            pstmt.setInt(2, 2);  // NEET Physics
            pstmt.setDate(3, java.sql.Date.valueOf("2025-01-20"));
            pstmt.setBigDecimal(4, new java.math.BigDecimal("12000.00"));
            pstmt.setString(5, "active");
            pstmt.executeUpdate();
            
            System.out.println("Sample enrollments data inserted successfully!");
            
            // Insert sample attendance
            String insertAttendance = "INSERT INTO attendance (student_id, course_id, date, status) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertAttendance);
            
            // Attendance for Rahul
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1);
            pstmt.setDate(3, java.sql.Date.valueOf("2025-02-19"));
            pstmt.setString(4, "present");
            pstmt.executeUpdate();
            
            // Attendance for Priya
            pstmt.setInt(1, 2);
            pstmt.setInt(2, 2);
            pstmt.setDate(3, java.sql.Date.valueOf("2025-02-19"));
            pstmt.setString(4, "present");
            pstmt.executeUpdate();
            
            System.out.println("Sample attendance data inserted successfully!");
            
            // Commit the transaction
            conn.commit();
            System.out.println("All sample data inserted successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error inserting sample data: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);  // Reset auto-commit mode
                    releaseConnection(conn);
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    // Example of how to use the database connection
    public static void demonstrateConnection() {
        DBConnection db = DBConnection.getInstance();
        Connection conn = null;
        try {
            conn = db.getConnection();
            // Use the connection here for database operations
            System.out.println("Connection successfully obtained!");
            
        } catch (SQLException e) {
            System.err.println("Error using connection: " + e.getMessage());
        } finally {
            if (conn != null) {
                db.releaseConnection(conn);
            }
        }
    }
    
    // Example usage method
    public static void main(String[] args) {
        DBConnection dbConnection = DBConnection.getInstance();
        try {
            // Create all database tables
            dbConnection.createTables();
            System.out.println("All tables created successfully!");
            // Insert sample data
            dbConnection.insertSampleData();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Shutdown the connection pool when application ends
            dbConnection.shutdown();
        }
    }
}
