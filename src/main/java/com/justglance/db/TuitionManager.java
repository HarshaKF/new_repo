package com.justglance.db;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TuitionManager {
    private final DBConnection dbConnection;

    public TuitionManager() {
        this.dbConnection = DBConnection.getInstance();
    }

    // Student Operations
    public int addStudent(String name, String email, String phone, String address, LocalDate dateOfBirth) throws SQLException {
        String sql = "INSERT INTO students (name, email, phone, address, date_of_birth) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, address);
            pstmt.setDate(5, Date.valueOf(dateOfBirth));
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to get student ID");
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) dbConnection.releaseConnection(conn);
        }
    }

    // Course Operations
    public int addCourse(String name, String description, String duration, BigDecimal fee, 
                        int maxStudents, String schedule) throws SQLException {
        String sql = "INSERT INTO courses (name, description, duration, fee, max_students, schedule) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, duration);
            pstmt.setBigDecimal(4, fee);
            pstmt.setInt(5, maxStudents);
            pstmt.setString(6, schedule);
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to get course ID");
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) dbConnection.releaseConnection(conn);
        }
    }

    // Enrollment Operations
    public void enrollStudent(int studentId, int courseId, BigDecimal feePaid) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrollment_date, fee_paid, status) " +
                    "VALUES (?, ?, ?, ?, 'active')";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            pstmt.setBigDecimal(4, feePaid);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) dbConnection.releaseConnection(conn);
        }
    }

    // Attendance Operations
    public void markAttendance(int studentId, int courseId, LocalDate date, String status) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, course_id, date, status) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setDate(3, Date.valueOf(date));
            pstmt.setString(4, status);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) dbConnection.releaseConnection(conn);
        }
    }

    // Query Operations
    public List<String> getStudentCourses(int studentId) throws SQLException {
        String sql = "SELECT c.name FROM courses c " +
                    "JOIN enrollments e ON c.id = e.course_id " +
                    "WHERE e.student_id = ? AND e.status = 'active'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> courses = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                courses.add(rs.getString("name"));
            }
            return courses;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) dbConnection.releaseConnection(conn);
        }
    }

    public double getStudentAttendancePercentage(int studentId, int courseId) throws SQLException {
        String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM attendance WHERE student_id = ? AND course_id = ? AND status = 'present') * 100.0 / " +
                    "(SELECT COUNT(*) FROM attendance WHERE student_id = ? AND course_id = ?) as percentage";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setInt(3, studentId);
            pstmt.setInt(4, courseId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("percentage");
            }
            return 0.0;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) dbConnection.releaseConnection(conn);
        }
    }

    // Example usage
    public static void main(String[] args) {
        TuitionManager manager = new TuitionManager();
        try {
            // Add a new student
            int studentId = manager.addStudent(
                "Amit Kumar", 
                "amit.kumar@email.com", 
                "9876543214", 
                "789 Lake Road, Bangalore", 
                LocalDate.of(2005, 3, 15)
            );
            System.out.println("Added new student with ID: " + studentId);

            // Add a new course
            int courseId = manager.addCourse(
                "NEET Chemistry",
                "Complete chemistry preparation for NEET",
                "6 months",
                new BigDecimal("13000.00"),
                25,
                "Mon, Wed, Fri - 2:00 PM to 4:00 PM"
            );
            System.out.println("Added new course with ID: " + courseId);

            // Enroll student in course
            manager.enrollStudent(studentId, courseId, new BigDecimal("13000.00"));
            System.out.println("Enrolled student in course");

            // Mark attendance
            manager.markAttendance(studentId, courseId, LocalDate.now(), "present");
            System.out.println("Marked attendance for student");

            // Get student's courses
            List<String> courses = manager.getStudentCourses(studentId);
            System.out.println("Student's courses: " + courses);

            // Get attendance percentage
            double attendance = manager.getStudentAttendancePercentage(studentId, courseId);
            System.out.println("Student's attendance percentage: " + attendance + "%");

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
