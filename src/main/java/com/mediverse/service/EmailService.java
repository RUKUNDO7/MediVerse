package com.mediverse.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[MediVerse] " + subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendAppointmentBookedEmail(String to, String patientName, String doctorName, String date, boolean isVirtual) {
        String type = isVirtual ? "Virtual" : "In-Person";
        String body = "<h2>Appointment Booked ✅</h2>"
                + "<p>Hi <strong>" + patientName + "</strong>,</p>"
                + "<p>Your <strong>" + type + "</strong> appointment with <strong>Dr. " + doctorName + "</strong> has been booked for <strong>" + date + "</strong>.</p>"
                + "<p>Status: <strong>Pending Approval</strong></p>"
                + "<br><p style='color:gray;font-size:12px;'>MediVerse Hospital Management System</p>";
        sendEmail(to, "Appointment Booked", body);
    }

    public void sendAppointmentStatusEmail(String to, String patientName, String status, String meetingLink) {
        String extra = "";
        if (meetingLink != null && !meetingLink.isEmpty()) {
            extra = "<p>Your meeting link: <a href='" + meetingLink + "'>" + meetingLink + "</a></p>";
        }
        String body = "<h2>Appointment Update 📋</h2>"
                + "<p>Hi <strong>" + patientName + "</strong>,</p>"
                + "<p>Your appointment status has been updated to: <strong>" + status + "</strong>.</p>"
                + extra
                + "<br><p style='color:gray;font-size:12px;'>MediVerse Hospital Management System</p>";
        sendEmail(to, "Appointment Status Updated", body);
    }

    public void sendPrescriptionIssuedEmail(String to, String patientName, String doctorName, String medications) {
        String body = "<h2>New Prescription 💊</h2>"
                + "<p>Hi <strong>" + patientName + "</strong>,</p>"
                + "<p>Dr. <strong>" + doctorName + "</strong> has issued you a prescription.</p>"
                + "<p><strong>Medications:</strong> " + medications + "</p>"
                + "<br><p style='color:gray;font-size:12px;'>MediVerse Hospital Management System</p>";
        sendEmail(to, "New Prescription Issued", body);
    }
}
