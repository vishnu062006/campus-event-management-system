package com.example.campus_events.service;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendRegistrationConfirmation(User user, Event event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("✅ Registered for " + event.getTitle());
            message.setText(
                    "Hi " + user.getName() + ",\n\n" +
                            "You've successfully registered for:\n\n" +
                            "📌 Event: " + event.getTitle() + "\n" +
                            "📍 Location: " + event.getLocation() + "\n" +
                            "📅 Date: " + event.getEventDate().toLocalDate() + "\n" +
                            (event.getEntryFee() > 0
                                    ? "💰 Fee paid: ₹" + event.getEntryFee() + "\n"
                                    : "🆓 Free event\n") +
                            "\nSee you there!\n— Eventara"
            );
            mailSender.send(message);
            log.info("Registration email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", user.getEmail(), e.getMessage());
            // Don't throw — email failure should never block registration
        }
    }

    public void sendCancellationConfirmation(User user, Event event, boolean refunded) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("❌ Registration Cancelled — " + event.getTitle());
            message.setText(
                    "Hi " + user.getName() + ",\n\n" +
                            "Your registration for \"" + event.getTitle() + "\" has been cancelled.\n" +
                            (refunded && event.getEntryFee() > 0
                                    ? "💸 ₹" + event.getEntryFee() + " has been refunded to your wallet.\n"
                                    : "") +
                            "\n— Eventara"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }

    public void sendWaitlistPromotion(User user, Event event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("🎉 You're in! Promoted from waitlist — " + event.getTitle());
            message.setText(
                    "Hi " + user.getName() + ",\n\n" +
                            "Great news! A spot opened up and you've been automatically registered for:\n\n" +
                            "📌 " + event.getTitle() + "\n" +
                            "📍 " + event.getLocation() + "\n" +
                            "📅 " + event.getEventDate().toLocalDate() + "\n" +
                            (event.getEntryFee() > 0
                                    ? "💰 ₹" + event.getEntryFee() + " deducted from your wallet.\n"
                                    : "") +
                            "\n— Eventara"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send waitlist promotion email: {}", e.getMessage());
        }
    }

    public void sendWaitlistJoined(User user, Event event, int position) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("⏳ Added to waitlist — " + event.getTitle());
            message.setText(
                    "Hi " + user.getName() + ",\n\n" +
                            "The event \"" + event.getTitle() + "\" is currently full.\n" +
                            "You've been added to the waitlist at position #" + position + ".\n\n" +
                            "We'll automatically register you and notify you if a spot opens.\n\n" +
                            "— Eventara"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send waitlist email: {}", e.getMessage());
        }
    }
}