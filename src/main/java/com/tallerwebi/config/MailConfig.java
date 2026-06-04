package com.tallerwebi.config;

import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(obtenerEnv("MAILTRAP_HOST", "sandbox.smtp.mailtrap.io"));
    mailSender.setPort(Integer.parseInt(obtenerEnv("MAILTRAP_PORT", "2525")));
    mailSender.setUsername(obtenerEnv("MAILTRAP_USER", "13b6c5f52b7e9e"));
    mailSender.setPassword(obtenerEnv("MAILTRAP_PASSWORD", "c9041c1603f335"));
    mailSender.setDefaultEncoding("UTF-8");

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "false");
    return mailSender;
  }

  private String obtenerEnv(String key, String defaultValue) {
    String value = System.getenv(key);
    return value != null && !value.trim().isEmpty() ? value : defaultValue;
  }
}
