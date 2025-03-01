package com.justglance.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.time.LocalDateTime;

public class EmailService {
    private final String username = System.getenv("EMAIL_USERNAME"); // Corrected environment variable
    private final String password = System.getenv("EMAIL_PASSWORD"); // Corrected environment variable
    private final Session session;

    public EmailService() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        
        session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendEnrollmentConfirmation(String studentName, String studentEmail, 
                                         String courseName, String schedule) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(studentEmail));
            message.setSubject("Welcome to Just Glance Tuition - Enrollment Confirmation");

            String htmlContent = String.format("""
                <html>
                <body style='font-family: Arial, sans-serif;'>
                    <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
                        <h2 style='color: #2c3e50;'>Welcome to Just Glance Tuition!</h2>
                        <p>Dear %s,</p>
                        <p>Thank you for enrolling in our <strong>%s</strong> course. We're excited to have you join us!</p>
                        <div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px;'>
                            <h3 style='color: #2c3e50; margin-top: 0;'>Course Details:</h3>
                            <p><strong>Course:</strong> %s</p>
                            <p><strong>Schedule:</strong> %s</p>
                        </div>
                        <p>What's Next?</p>
                        <ul>
                            <li>You'll receive your study materials within 24 hours</li>
                            <li>Your first class is scheduled as per the timetable</li>
                            <li>Please arrive 15 minutes early for your first class</li>
                        </ul>
                        <p>If you have any questions, feel free to reply to this email or call us.</p>
                        <p>Best regards,<br>Just Glance Tuition Team</p>
                    </div>
                </body>
                </html>
                """, studentName, courseName, courseName, schedule);

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    public void sendAdminNotification(String studentName, String courseName) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
            message.setSubject("New Enrollment Alert - Just Glance Tuition");

            String htmlContent = String.format("""
                <html>
                <body style='font-family: Arial, sans-serif;'>
                    <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
                        <h2 style='color: #2c3e50;'>New Enrollment Alert!</h2>
                        <p>A new student has enrolled in a course:</p>
                        <div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px;'>
                            <p><strong>Student Name:</strong> %s</p>
                            <p><strong>Course:</strong> %s</p>
                            <p><strong>Enrollment Time:</strong> %s</p>
                        </div>
                        <p>Please check the admin dashboard for complete details.</p>
                    </div>
                </body>
                </html>
                """, studentName, courseName, LocalDateTime.now());

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send admin notification: " + e.getMessage(), e);
        }
    }
}