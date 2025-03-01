package com.justglance.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EnrollmentRequestTest {

    @Test
    void testEnrollmentRequest_ValidData() {
        // Arrange
        EnrollmentRequest request = new EnrollmentRequest();
        
        // Act
        request.setStudentName("Test Student");
        request.setStudentEmail("test@example.com");
        request.setStudentPhone("9876543210");
        request.setStudentDOB(LocalDate.of(2005, 1, 1));
        request.setStudentAddress("123 Test Street");
        request.setCourseName("JEE Advanced");
        request.setCourseFee("₹45,000");
        request.setCourseDuration("12 months");
        request.setCourseSchedule("Mon/Wed/Fri");
        request.setPreviousSchool("Test School");
        request.setPreviousGrade("95%");

        // Assert
        assertEquals("Test Student", request.getStudentName());
        assertEquals("test@example.com", request.getStudentEmail());
        assertEquals("9876543210", request.getStudentPhone());
        assertEquals(LocalDate.of(2005, 1, 1), request.getStudentDOB());
        assertEquals("123 Test Street", request.getStudentAddress());
        assertEquals("JEE Advanced", request.getCourseName());
        assertEquals("₹45,000", request.getCourseFee());
        assertEquals("12 months", request.getCourseDuration());
        assertEquals("Mon/Wed/Fri", request.getCourseSchedule());
        assertEquals("Test School", request.getPreviousSchool());
        assertEquals("95%", request.getPreviousGrade());
    }

    @Test
    void testEnrollmentRequest_NullValues() {
        // Arrange
        EnrollmentRequest request = new EnrollmentRequest();

        // Assert
        assertNull(request.getStudentName());
        assertNull(request.getStudentEmail());
        assertNull(request.getStudentPhone());
        assertNull(request.getStudentDOB());
        assertNull(request.getStudentAddress());
        assertNull(request.getCourseName());
        assertNull(request.getCourseFee());
        assertNull(request.getCourseDuration());
        assertNull(request.getCourseSchedule());
        assertNull(request.getPreviousSchool());
        assertNull(request.getPreviousGrade());
    }
}
