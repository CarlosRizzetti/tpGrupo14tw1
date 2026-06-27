package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioValidacionIdentidad;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("servicioValidacionIdentidad")
@Transactional
public class ServicioValidacionIdentidadImpl implements ServicioValidacionIdentidad {

  private static final Logger LOGGER = Logger.getLogger(
    ServicioValidacionIdentidadImpl.class.getName()
  );

  private final RepositorioUsuario repositorioUsuario;
  private final JavaMailSender mailSender;
  private final String baseUrl;

  @Autowired
  public ServicioValidacionIdentidadImpl(
    RepositorioUsuario repositorioUsuario,
    JavaMailSender mailSender
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.mailSender = mailSender;
    this.baseUrl = normalizarBaseUrl(obtenerEnv("APP_BASE_URL", "http://localhost:8080"));
  }

  @Override
  public void solicitarValidacion(String email) {
    String token = UUID.randomUUID().toString();
    Usuario usuario = repositorioUsuario.buscar(email);
    if (usuario != null) {
      usuario.setTokenValidacion(token);
      repositorioUsuario.modificar(usuario);
    }
    enviarCorreoValidacion(email, token);
  }

  @Override
  public boolean validarToken(String token) {
    if (token == null || token.trim().isEmpty()) {
      return false;
    }
    Usuario usuario = repositorioUsuario.buscarPorTokenValidacion(token);
    if (usuario == null) {
      return false;
    }
    // usuario.setActivo(true);
    usuario.setTokenValidacion(null);
    repositorioUsuario.modificar(usuario);
    return true;
  }

  private void enviarCorreoValidacion(String email, String token) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("Validación de identidad");
    message.setText("Para validar tu identidad, ingresa el siguiente token: " + token);
    try {
      mailSender.send(message);
    } catch (MailException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.warning("No se pudo enviar el correo de validación: " + e.getMessage());
      }
    }
  }

  private String obtenerEnv(String key, String defaultValue) {
    String value = System.getenv(key);
    return value != null && !value.trim().isEmpty() ? value : defaultValue;
  }

  private String normalizarBaseUrl(String url) {
    if (url.endsWith("/")) {
      return url.substring(0, url.length() - 1);
    }
    return url;
  }
}
