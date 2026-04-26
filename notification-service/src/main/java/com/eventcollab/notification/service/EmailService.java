package com.eventcollab.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.mock}")
    private boolean mock;

    public void send(String to, String subject, String body) {
        if (mock) {
            log.info("=== EMAIL MOCK ===");
            log.info("To      : {}", to);
            log.info("Subject : {}", subject);
            log.info("Body    : {}", body);
            log.info("=================");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email envoye a {}", to);
        } catch (Exception e) {
            log.error("Erreur envoi email a {} : {}", to, e.getMessage());
        }
    }

    public void sendTicketBooked(String to, String eventTitle) {
        send(
            to,
            "Votre billet pour " + eventTitle,
            "Bonjour,\n\nVotre billet pour \"" + eventTitle +
            "\" a ete reserve avec succes.\n\nA bientot sur EventCollab !"
        );
    }

    public void sendTicketCancelled(String to, String eventTitle) {
        send(
            to,
            "Annulation de votre billet - " + eventTitle,
            "Bonjour,\n\nVotre billet pour \"" + eventTitle +
            "\" a ete annule.\n\nEventCollab"
        );
    }

    public void sendEventCancelled(String to, String eventTitle) {
        send(
            to,
            "Evenement annule - " + eventTitle,
            "Bonjour,\n\nNous vous informons que l evenement \"" + eventTitle +
            "\" a ete annule par l organisateur.\n\nEventCollab"
        );
    }
}