package com.emailsender.service;

import com.emailsender.model.EmailFile;
import com.emailsender.model.GmailCredentials;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
public class EmailSenderService {

    private final EmailFileParser parser;

    public EmailSenderService(EmailFileParser parser) {
        this.parser = parser;
    }

    public List<EmailFile> loadEmails(String directory) throws Exception {
        return parser.parseDirectory(directory);
    }

    public void sendAll(List<EmailFile> emails, GmailCredentials credentials) {
        JavaMailSenderImpl mailSender = buildMailSender(credentials);
        for (EmailFile email : emails) {
            if ("pending".equals(email.getStatus())) {
                send(email, credentials.getUsername(), mailSender);
            }
        }
    }

    public void sendOne(EmailFile email, GmailCredentials credentials) {
        JavaMailSenderImpl mailSender = buildMailSender(credentials);
        send(email, credentials.getUsername(), mailSender);
    }

    private void send(EmailFile email, String from, JavaMailSenderImpl mailSender) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email.getTo());
            message.setSubject(email.getSubject());
            message.setText(email.getBody());
            mailSender.send(message);
            email.setStatus("sent");
        } catch (Exception e) {
            email.setStatus("failed");
            email.setErrorMessage(e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private JavaMailSenderImpl buildMailSender(GmailCredentials credentials) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername(credentials.getUsername());
        sender.setPassword(credentials.getAppPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        return sender;
    }
}
