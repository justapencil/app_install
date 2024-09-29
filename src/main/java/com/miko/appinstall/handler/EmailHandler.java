package com.miko.appinstall.handler;

import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class EmailHandler {

  private final String smtpHost;
  private final Integer smtpPort;
  private final String senderEmail;
  private final String senderPassword;

  public EmailHandler(String smtpHost, Integer smtpPort, String senderEmail, String senderPassword) {
    this.smtpHost = smtpHost;
    this.smtpPort = smtpPort;
    this.senderEmail = senderEmail;
    this.senderPassword = senderPassword;
  }

  public EmailHandler(Map<String, Object> smptConfig) {
    this.smtpHost = (String) smptConfig.get("host");
    this.smtpPort = (Integer) smptConfig.get("port");
    this.senderEmail = (String) smptConfig.get("email");
    this.senderPassword = (String) smptConfig.get("password");
  }

  public void sendEmail(String recipient, String subject, String messageBody) {
    try {

      Properties props = new Properties();
      props.put("mail.smtp.host", smtpHost);
      props.put("mail.smtp.port", smtpPort);
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");

      Session session = Session.getInstance(props, new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(senderEmail, senderPassword);
        }
      });

      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(senderEmail));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
      message.setSubject(subject);
      message.setText(messageBody);

      Transport.send(message);
      log.info("Email sent successfully to: {}", recipient);

    } catch (MessagingException e) {
      log.error("Failed to send email: {}", e.getMessage());
    }
  }
}
