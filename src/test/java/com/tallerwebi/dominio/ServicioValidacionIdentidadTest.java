package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioValidacionIdentidad;
import com.tallerwebi.dominio.services.ServicioValidacionIdentidadImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class ServicioValidacionIdentidadTest {

  private ServicioValidacionIdentidad servicioValidacionIdentidad;
  private RepositorioUsuario repositorioUsuarioMock;
  private JavaMailSender mailSenderMock;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    mailSenderMock = mock(JavaMailSender.class);
    servicioValidacionIdentidad =
      new ServicioValidacionIdentidadImpl(repositorioUsuarioMock, mailSenderMock);
  }

  @Test
  public void solicitarValidacionConUsuarioExistenteDeberiaGuardarTokenYEnviarCorreo() {
    String email = "test@test.com";
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    when(repositorioUsuarioMock.buscar(email)).thenReturn(usuario);

    servicioValidacionIdentidad.solicitarValidacion(email);

    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
    verify(repositorioUsuarioMock, times(1)).modificar(captor.capture());
    assertThat(captor.getValue().getTokenValidacion(), not(isEmptyOrNullString()));
    verify(mailSenderMock, times(1))
      .send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
  }

  @Test
  public void solicitarValidacionSinUsuarioDeberiaEnviarCorreoSinModificar() {
    String email = "noexiste@test.com";
    when(repositorioUsuarioMock.buscar(email)).thenReturn(null);

    servicioValidacionIdentidad.solicitarValidacion(email);

    verify(repositorioUsuarioMock, never())
      .modificar(org.mockito.ArgumentMatchers.any(Usuario.class));
    verify(mailSenderMock, times(1))
      .send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
  }

  @Test
  public void validarTokenInexistenteDeberiaRetornarFalse() {
    when(repositorioUsuarioMock.buscarPorTokenValidacion("token")).thenReturn(null);

    boolean activado = servicioValidacionIdentidad.validarToken("token");

    assertThat(activado, is(false));
    verify(repositorioUsuarioMock, never())
      .modificar(org.mockito.ArgumentMatchers.any(Usuario.class));
  }

  @Test
  public void validarTokenVacioDeberiaRetornarFalseSinConsultarRepositorio() {
    boolean activado = servicioValidacionIdentidad.validarToken(" ");

    assertThat(activado, is(false));
    verify(repositorioUsuarioMock, never())
      .buscarPorTokenValidacion(org.mockito.ArgumentMatchers.anyString());
  }
}
