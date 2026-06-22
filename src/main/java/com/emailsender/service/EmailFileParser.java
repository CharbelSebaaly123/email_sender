package com.emailsender.service;

import com.emailsender.model.EmailFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class EmailFileParser {

    /**
     * Parses all .email files from the given directory.
     * File format:
     *   to: recipient@example.com
     *   subject: Your Subject Here
     *   (blank line)
     *   Body text follows...
     */
    public List<EmailFile> parseDirectory(String directoryPath) throws IOException {
        List<EmailFile> emails = new ArrayList<>();
        Path dir = resolveDirectory(directoryPath);

        try (Stream<Path> files = Files.walk(dir, 1)) {
            files.filter(p -> p.toString().endsWith(".email") && Files.isRegularFile(p))
                 .forEach(p -> {
                     try {
                         emails.add(parseFile(p));
                     } catch (IOException e) {
                         EmailFile bad = new EmailFile();
                         bad.setFilename(p.getFileName().toString());
                         bad.setStatus("failed");
                         bad.setErrorMessage("Parse error: " + e.getMessage());
                         emails.add(bad);
                     }
                 });
        }
        return emails;
    }

    public EmailFile parseFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        String to = "";
        String subject = "";
        StringBuilder body = new StringBuilder();
        boolean inBody = false;

        for (String line : lines) {
            if (!inBody) {
                if (line.toLowerCase().startsWith("to:")) {
                    to = line.substring(3).trim();
                } else if (line.toLowerCase().startsWith("subject:")) {
                    subject = line.substring(8).trim();
                } else if (line.trim().isEmpty()) {
                    inBody = true;
                }
            } else {
                if (body.length() > 0) body.append("\n");
                body.append(line);
            }
        }

        return new EmailFile(filePath.getFileName().toString(), to, subject, body.toString().trim());
    }

    private Path resolveDirectory(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!path.isAbsolute()) {
            // Try classpath resources first, then working directory
            Path classpathDir = Paths.get(
                getClass().getClassLoader().getResource("").toExternalForm()
                    .replace("file:", "") + directoryPath);
            if (Files.isDirectory(classpathDir)) return classpathDir;
            path = Paths.get(System.getProperty("user.dir")).resolve(directoryPath);
        }
        if (!Files.isDirectory(path)) {
            throw new IOException("Directory not found: " + path.toAbsolutePath());
        }
        return path;
    }
}
