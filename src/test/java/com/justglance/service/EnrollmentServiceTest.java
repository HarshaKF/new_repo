package com.justglance.service;

import com.justglance.db.TuitionManager;
import com.justglance.model.EnrollmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private TuitionManager tuitionManager;

    private EnrollmentService enrollmentService;
    private EnrollmentRequest validRequest;

    @BeforeEach
    void setUp() {
        // Initialize service with mocked TuitionManager
        enrollmentService = new EnrollmentService(tuitionManager);

        // Create a valid enrollment request for testing
        validRequest = new EnrollmentRequest();
        validRequest.setStudentName("Test Student");
        validRequest.setStudentEmail("test@example.com");
        validRequest.setStudentPhone("9876543210");
        validRequest.setStudentAddress("123 Test Street");
        validRequest.setCourseName("NEET Chemistry");
        validRequest.setStudentDOB(LocalDate.of(2005, 1, 1));
        validRequest.setCourseFee("₹15,000");
    }

    @Test
    void processEnrollment_SuccessfulEnrollment() throws SQLException {
        // Arrange
        when(tuitionManager.addStudent(any(), any(), any(), any(), any())).thenReturn(1);
        doThrow(new SQLException("Enrollment failed")).when(tuitionManager).enrollStudent(anyInt(), anyInt(), any());

        // Act
        int studentId = enrollmentService.processEnrollment(validRequest);

        // Assert
        assertEquals(1, studentId);
        verify(tuitionManager).addStudent(any(), any(), any(), any(), any());
        verify(tuitionManager).enrollStudent(anyInt(), anyInt(), any());
    }

    @Test
    void processEnrollment_InvalidCourse_ThrowsSQLException() throws SQLException {
        // Arrange
        validRequest.setCourseName("Invalid Course");
        when(tuitionManager.addStudent(any(), any(), any(), any(), any())).thenReturn(1);
        doThrow(new SQLException("Invalid course")).when(tuitionManager).enrollStudent(anyInt(), anyInt(), any());

        // Act & Assert
        assertThrows(SQLException.class, () -> 
            enrollmentService.processEnrollment(validRequest)
        );
    }

    @Test
    void processEnrollment_NullRequest_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            enrollmentService.processEnrollment(null)
        );
    }
}
