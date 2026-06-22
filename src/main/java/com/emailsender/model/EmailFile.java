package com.emailsender.model;

public class EmailFile {
    private String filename;
    private String to;
    private String subject;
    private String body;
    private String status; // "pending", "sent", "failed"
    private String errorMessage;

    public EmailFile() {}

    public EmailFile(String filename, String to, String subject, String body) {
        this.filename = filename;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.status = "pending";
    }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
