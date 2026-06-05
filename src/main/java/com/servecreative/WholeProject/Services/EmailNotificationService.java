package com.servecreative.WholeProject.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean mailEnabled) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
    }

    public void sendRideConfirmation(String toEmail, int dutyId, String pickup, String drop, double fare) {
        String subject = "CabsNow — Ride Booked #" + dutyId;
        String body = "Your ride is confirmed!\n\nPickup: " + pickup
                + "\nDrop: " + drop + "\nFare: ₹" + fare
                + "\n\nTrack status in the app.";
        send(toEmail, subject, body);
    }

    public void sendRideStatusUpdate(String toEmail, int dutyId, String status) {
        send(toEmail, "CabsNow — Ride #" + dutyId + " " + status,
                "Your ride status is now: " + status);
    }

    private void send(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[Email mock] To: {} | Subject: {} | Body: {}", to, subject, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
