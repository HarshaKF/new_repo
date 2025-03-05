package com.justglance.controller;

import com.justglance.model.ApiResponse;
import com.justglance.model.EnrollmentRequest;
import com.justglance.service.EnrollmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet("/api/enroll")
public class EnrollmentController extends HttpServlet {
    private final EnrollmentService enrollmentService;
    private final Gson gson;

    public EnrollmentController() {
        this.enrollmentService = createEnrollmentService();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    }

    // Protected factory method for testing
    protected EnrollmentService createEnrollmentService() {
        return new EnrollmentService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Read request body
            BufferedReader reader = request.getReader();
            EnrollmentRequest enrollmentRequest = gson.fromJson(reader, EnrollmentRequest.class);

            // Validate request
            validateRequest(enrollmentRequest);

            // Process enrollment
            int studentId = enrollmentService.processEnrollment(enrollmentRequest);

            // Send success response
            ApiResponse apiResponse = new ApiResponse(true, 
                "Enrollment successful! Your student ID is: " + studentId);
            response.getWriter().write(gson.toJson(apiResponse));

        } catch (Exception e) {
            // Send error response
            ApiResponse apiResponse = new ApiResponse(false, 
                "Enrollment failed: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(apiResponse));
        }
    }

    private void validateRequest(EnrollmentRequest request) throws IllegalArgumentException {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (isEmpty(request.getStudentName())) {
            throw new IllegalArgumentException("Student name is required");
        }
        if (isEmpty(request.getStudentEmail())) {
            throw new IllegalArgumentException("Student email is required");
        }
        if (isEmpty(request.getStudentPhone())) {
            throw new IllegalArgumentException("Student phone is required");
        }
        if (request.getStudentDOB() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        if (isEmpty(request.getStudentAddress())) {
            throw new IllegalArgumentException("Student address is required");
        }
        if (isEmpty(request.getCourseName())) {
            throw new IllegalArgumentException("Course name is required");
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}

class LocalDateAdapter implements com.google.gson.JsonSerializer<LocalDate>, 
                                com.google.gson.JsonDeserializer<LocalDate> {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
            com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
        return LocalDate.parse(json.getAsString(), formatter);
    }

    @Override
    public com.google.gson.JsonElement serialize(LocalDate src, java.lang.reflect.Type typeOfSrc,
            com.google.gson.JsonSerializationContext context) {
        return new com.google.gson.JsonPrimitive(formatter.format(src));
    }
}
