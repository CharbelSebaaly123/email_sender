package com.emailsender.controller;

import com.emailsender.model.EmailFile;
import com.emailsender.model.GmailCredentials;
import com.emailsender.service.EmailSenderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EmailController {

    private final EmailSenderService emailSenderService;

    @Value("${email.files.directory:emails}")
    private String defaultEmailDirectory;

    public EmailController(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        GmailCredentials credentials = getSessionCredentials(session);
        model.addAttribute("credentials", credentials);
        model.addAttribute("defaultDirectory", defaultEmailDirectory);
        return "index";
    }

    @PostMapping("/load")
    public String loadEmails(@ModelAttribute GmailCredentials credentials,
                             @RequestParam(defaultValue = "") String emailDirectory,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        session.setAttribute("credentials", credentials);
        String dir = emailDirectory.isBlank() ? defaultEmailDirectory : emailDirectory;
        session.setAttribute("emailDirectory", dir);

        try {
            List<EmailFile> emails = emailSenderService.loadEmails(dir);
            session.setAttribute("emails", emails);
            redirectAttributes.addFlashAttribute("info", "Loaded " + emails.size() + " email file(s) from: " + dir);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to load emails: " + e.getMessage());
        }
        return "redirect:/emails";
    }

    @GetMapping("/emails")
    public String showEmails(Model model, HttpSession session) {
        List<EmailFile> emails = getSessionEmails(session);
        GmailCredentials credentials = getSessionCredentials(session);
        model.addAttribute("emails", emails);
        model.addAttribute("credentials", credentials);
        model.addAttribute("emailDirectory", session.getAttribute("emailDirectory"));
        return "emails";
    }

    @PostMapping("/send-all")
    public String sendAll(HttpSession session, RedirectAttributes redirectAttributes) {
        List<EmailFile> emails = getSessionEmails(session);
        GmailCredentials credentials = getSessionCredentials(session);

        if (emails == null || emails.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No emails loaded.");
            return "redirect:/emails";
        }
        if (credentials == null || credentials.getUsername() == null) {
            redirectAttributes.addFlashAttribute("error", "Please enter Gmail credentials first.");
            return "redirect:/";
        }

        emailSenderService.sendAll(emails, credentials);
        long sent = emails.stream().filter(e -> "sent".equals(e.getStatus())).count();
        long failed = emails.stream().filter(e -> "failed".equals(e.getStatus())).count();
        redirectAttributes.addFlashAttribute("info", "Sent: " + sent + " | Failed: " + failed);
        return "redirect:/emails";
    }

    @PostMapping("/send-one/{index}")
    public String sendOne(@PathVariable int index,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        List<EmailFile> emails = getSessionEmails(session);
        GmailCredentials credentials = getSessionCredentials(session);

        if (emails == null || index < 0 || index >= emails.size()) {
            redirectAttributes.addFlashAttribute("error", "Invalid email index.");
            return "redirect:/emails";
        }

        emailSenderService.sendOne(emails.get(index), credentials);
        EmailFile email = emails.get(index);
        if ("sent".equals(email.getStatus())) {
            redirectAttributes.addFlashAttribute("info", "Email sent: " + email.getSubject());
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed: " + email.getErrorMessage());
        }
        return "redirect:/emails";
    }

    @SuppressWarnings("unchecked")
    private List<EmailFile> getSessionEmails(HttpSession session) {
        return (List<EmailFile>) session.getAttribute("emails");
    }

    private GmailCredentials getSessionCredentials(HttpSession session) {
        GmailCredentials creds = (GmailCredentials) session.getAttribute("credentials");
        return creds != null ? creds : new GmailCredentials();
    }
}
