package com.justglance.model;

import java.time.LocalDate;

public class EnrollmentRequest {
    private String courseName;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private LocalDate studentDOB;
    private String studentAddress;
    private String previousSchool;
    private String previousGrade;
    private String courseFee;
    private String courseDuration;
    private String courseSchedule;

    // Getters and Setters
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getStudentPhone() { return studentPhone; }
    public void setStudentPhone(String studentPhone) { this.studentPhone = studentPhone; }

    public LocalDate getStudentDOB() { return studentDOB; }
    public void setStudentDOB(LocalDate studentDOB) { this.studentDOB = studentDOB; }

    public String getStudentAddress() { return studentAddress; }
    public void setStudentAddress(String studentAddress) { this.studentAddress = studentAddress; }

    public String getPreviousSchool() { return previousSchool; }
    public void setPreviousSchool(String previousSchool) { this.previousSchool = previousSchool; }

    public String getPreviousGrade() { return previousGrade; }
    public void setPreviousGrade(String previousGrade) { this.previousGrade = previousGrade; }

    public String getCourseFee() { return courseFee; }
    public void setCourseFee(String courseFee) { this.courseFee = courseFee; }

    public String getCourseDuration() { return courseDuration; }
    public void setCourseDuration(String courseDuration) { this.courseDuration = courseDuration; }

    public String getCourseSchedule() { return courseSchedule; }
    public void setCourseSchedule(String courseSchedule) { this.courseSchedule = courseSchedule; }
}
