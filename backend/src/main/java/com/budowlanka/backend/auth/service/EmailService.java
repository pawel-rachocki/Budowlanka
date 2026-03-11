package com.budowlanka.backend.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Async
  public void sendVerificationEmail(String toEmail, String verificationLink) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(toEmail);
      helper.setSubject("Aktywuj swoje konto — Portal Ekipy Remontowe");
      helper.setFrom("noreply@budowlanka.pl");
      helper.setText(buildTextBody(verificationLink), buildHtmlBody(verificationLink));
      mailSender.send(message);
      log.info("Verification email sent to {}", toEmail);
    } catch (MessagingException e) {
      log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
    }
  }

  private String buildHtmlBody(String link) {
    return """
        <html>
        <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <h2>Aktywuj konto</h2>
          <p>Dziękujemy za rejestrację w Portalu Ekipy Remontowe.</p>
          <p>Kliknij poniższy przycisk, aby aktywować swoje konto:</p>
          <a href="%s"
             style="display:inline-block;padding:12px 24px;background:#2563eb;color:#fff;
                    text-decoration:none;border-radius:6px;font-weight:bold;">
            Aktywuj konto
          </a>
          <p style="margin-top:16px;color:#6b7280;font-size:14px;">
            Lub skopiuj link: <a href="%s">%s</a>
          </p>
          <p style="color:#6b7280;font-size:12px;">
            Link jest ważny przez 24 godziny.
          </p>
        </body>
        </html>
        """
        .formatted(link, link, link);
  }

  private String buildTextBody(String link) {
    return "Aktywuj konto klikając w link: " + link + "\n\nLink jest ważny przez 24 godziny.";
  }
}
