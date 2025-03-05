package com.justglance.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.justglance.model.EnrollmentRequest;
import com.justglance.service.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentControllerTest {

    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private EnrollmentController controller;
    private StringWriter responseWriter;
    private Gson gson;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize controller with mocked service
        controller = new EnrollmentController() {
            @Override
            protected EnrollmentService createEnrollmentService() {
                return enrollmentService;
            }
        };

        // Setup response writer
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Setup Gson
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // Ensure LocalDateAdapter exists
            .create();
    }

    @Test
    void doPost_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        EnrollmentRequest validRequest = new EnrollmentRequest();
        validRequest.setStudentName("Test Student");
        validRequest.setStudentEmail("test@example.com");
        validRequest.setStudentPhone("9876543210");
        validRequest.setStudentAddress("123 Test Street");
        validRequest.setStudentDOB(LocalDate.of(2005, 1, 1));
        validRequest.setCourseName("JEE Advanced");
        validRequest.setCourseFee("₹45,000");

        String jsonRequest = gson.toJson(validRequest);
        BufferedReader reader = new BufferedReader(new StringReader(jsonRequest));
        when(request.getReader()).thenReturn(reader);
        when(enrollmentService.processEnrollment(any())).thenReturn(1);

        // Act
        controller.doPost(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(enrollmentService).processEnrollment(any());
        
        String responseJson = responseWriter.toString();
        assertTrue(responseJson.contains("\"success\":true"));
        assertTrue(responseJson.contains("student ID is: 1"));
    }

    @Test
    void doPost_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange
        EnrollmentRequest invalidRequest = new EnrollmentRequest();
        // Missing required fields
        String jsonRequest = gson.toJson(invalidRequest);
        BufferedReader reader = new BufferedReader(new StringReader(jsonRequest));
        when(request.getReader()).thenReturn(reader);

        // Act
        controller.doPost(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String responseJson = responseWriter.toString();
        assertTrue(responseJson.contains("\"success\":false"));
        assertTrue(responseJson.contains("Student name is required"));
    }

    @Test
    void doPost_DatabaseError_ReturnsBadRequest() throws Exception {
        // Arrange
        EnrollmentRequest validRequest = new EnrollmentRequest();
        validRequest.setStudentName("Test Student");
        validRequest.setStudentEmail("test@example.com");
        validRequest.setStudentPhone("9876543210");
        validRequest.setStudentAddress("123 Test Street");
        validRequest.setStudentDOB(LocalDate.of(2005, 1, 1));
        validRequest.setCourseName("JEE Advanced");
        validRequest.setCourseFee("₹45,000");

        String jsonRequest = gson.toJson(validRequest);
        BufferedReader reader = new BufferedReader(new StringReader(jsonRequest));
        when(request.getReader()).thenReturn(reader);
        when(enrollmentService.processEnrollment(any()))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        controller.doPost(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String responseJson = responseWriter.toString();
        assertTrue(responseJson.contains("\"success\":false"));
        assertTrue(responseJson.contains("Database error"));
    }

    @Test
    void doPost_InvalidJson_ReturnsBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{invalid json}";
        BufferedReader reader = new BufferedReader(new StringReader(invalidJson));
        when(request.getReader()).thenReturn(reader);

        // Act
        controller.doPost(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String responseJson = responseWriter.toString();
        assertTrue(responseJson.contains("\"success\":false"));
    }

    @Test
    void doPost_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        // Arrange
        EnrollmentRequest incompleteRequest = new EnrollmentRequest();
        incompleteRequest.setStudentName(""); // Empty name
        incompleteRequest.setStudentEmail(""); // Empty email
        incompleteRequest.setStudentPhone(""); // Empty phone
        
        String jsonRequest = gson.toJson(incompleteRequest);
        BufferedReader reader = new BufferedReader(new StringReader(jsonRequest));
        when(request.getReader()).thenReturn(reader);

        // Act
        controller.doPost(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String responseJson = responseWriter.toString();
        assertTrue(responseJson.contains("\"success\":false"));
        assertTrue(responseJson.contains("Student name is required"));
        assertTrue(responseJson.contains("Student email is required"));
        assertTrue(responseJson.contains("Student phone is required"));
    }

    @Test
    void doPost_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        EnrollmentRequest invalidEmailRequest = new EnrollmentRequest();
        invalidEmailRequest.setStudentName("Test Student");
        invalidEmailRequest.setStudentEmail("invalid-email"); // Invalid email format
        invalidEmailRequest.setStudentPhone("9876543210");
        invalidEmailRequest.setStudentAddress("123 Test Street");
        invalidEmailRequest.setStudentDOB(LocalDate.of(2005, 1, 1));
        invalidEmailRequest.setCourseName("JEE Advanced");
        invalidEmailRequest.setCourseFee("₹45,000");

        String jsonRequest = gson.toJson(invalidEmailRequest);
        BufferedReader reader = new BufferedReader(new StringReader(jsonRequest));
        when(request.getReader()).thenReturn(reader);

        // Act
        controller.doPost(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String responseJson = responseWriter.toString();
        assertTrue(responseJson.contains("\"success\":false"));
        assertTrue(responseJson.contains("Invalid email format"));
    }

    @Test
    void doPost_InvalidPhoneNumber_ReturnsBadRequest() throws Exception {
        // Arrange
        EnrollmentRequest invalidPhoneRequest = new EnrollmentRequest();
        invalidPhoneRequest.setStudentName("Test Student");
        invalidPhoneRequest.setStudentEmail("test@example.com");
        invalidPhoneRequest.setStudentPhone("123"); // Invalid phone number
        invalidPhoneRequest.setStudentAddress("123 Test Street");
        invalidPhoneRequest.setStudentDOB(LocalDate.of(2005, 1, 1));
        invalidPhoneRequest.setCourseName("JEE Advanced");
        invalidPhoneRequest.setCourseFee("₹45,000");

        String jsonRequest = gson.toJson(invalidPhoneRequest);
        BufferedReader reader = new BufferedReader(new StringReader(jsonRequest));
        when(request.getReader()).thenReturn(reader);

        // Act
        controller.doPost(request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String responseJson = responseWriter.toString();
        assertTrue(responseJson.contains("\"success\":false"));
        assertTrue(responseJson.contains("Invalid phone number format"));
    }
}
