package com.emailsender.model;

public class GmailCredentials {
    private String username;
    private String appPassword;
    private String emailDirectory;

    public GmailCredentials() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAppPassword() { return appPassword; }
    public void setAppPassword(String appPassword) { this.appPassword = appPassword; }

    public String getEmailDirectory() { return emailDirectory; }
    public void setEmailDirectory(String emailDirectory) { this.emailDirectory = emailDirectory; }
}
