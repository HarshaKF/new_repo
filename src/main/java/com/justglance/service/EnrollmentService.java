package com.justglance.service;

import com.justglance.db.TuitionManager;
import com.justglance.model.EnrollmentRequest;
import java.sql.SQLException;
import java.math.BigDecimal;

public class EnrollmentService {
    private final TuitionManager tuitionManager;

    //Constructor with depende
    public EnrollmentService() {
        this.tuitionManager = createTuitionManager();
    }

    //Default constructor for backward compatibility
    public EnrollmentService(TuitionManager tuitionManager) {
        this.tuitionManager = tuitionManager;
    }

    // Protected factory method for testing
    protected TuitionManager createTuitionManager() {
        return new TuitionManager();
    }

    public int processEnrollment(EnrollmentRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("Enrollment request cannot be null");
        }

        // 1. Add student to database
        int studentId = tuitionManager.addStudent(
            request.getStudentName(),
            request.getStudentEmail(),
            request.getStudentPhone(),
            request.getStudentAddress(),
            request.getStudentDOB()
        );

        // 2. Get course ID (assuming course already exists)
        int courseId = getCourseIdByName(request.getCourseName());

        // 3. Create enrollment
        String feeStr = request.getCourseFee().replace("₹", "").replace(",", "");
        BigDecimal feePaid = new BigDecimal(feeStr);
        tuitionManager.enrollStudent(studentId, courseId, feePaid);

        return studentId;
    }

    private int getCourseIdByName(String courseName) throws SQLException {
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new SQLException("Course name cannot be empty");
        }

        // In a real application, you would query the database
        // For now, we'll use a simple switch case
        switch(courseName) {
            case "JEE Advanced": return 1;
            case "NEET Preparation": return 2;
            case "Foundation Course": return 3;
            case "Board Exam Preparation": return 4;
            case "Crash Course": return 5;
            case "Olympiad Training": return 6;
            default: throw new SQLException("Course not found: " + courseName);
        }
    }
}