package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInactivo;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import com.tallerwebi.dominio.services.ServicioLoginImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ServicioLoginTest {

  private ServicioLogin servicioLogin;
  private RepositorioUsuario repositorioUsuarioMock;
  private BCryptPasswordEncoder passwordEncoderMock;

  @BeforeEach
  public void init() {
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.passwordEncoderMock = mock(BCryptPasswordEncoder.class);
    this.servicioLogin = new ServicioLoginImpl(this.repositorioUsuarioMock, passwordEncoderMock);
  }

  @Test
  public void consultarUsuarioDeberiaLlamarAlRepositorio()
    throws UsuarioInactivo, PasswordInvalida {
    // preparacion
    String email = "test@test.com";
    String password = "Valida123"; // Cumple con los nuevos requisitos
    Usuario usuarioEsperado = new Usuario();
    usuarioEsperado.setEmail(email);
    usuarioEsperado.setPassword(password);
    usuarioEsperado.setActivo(true);
    when(this.repositorioUsuarioMock.buscarUsuario(email)).thenReturn(usuarioEsperado);
    when(this.passwordEncoderMock.matches(any(), anyString())).thenReturn(true);
    // ejecucion
    Usuario usuarioObtenido = this.servicioLogin.consultarUsuario(email, password);

    // validacion
    assertThat(usuarioObtenido, equalTo(usuarioEsperado));
    verify(this.repositorioUsuarioMock, times(1)).buscarUsuario(email);
  }

  @Test
  public void consultarUsuarioDeberialanzarExcepcionPasswordInvalidaSiElRepositorioNoEncuentraAlUsuario()
    throws UsuarioInactivo, PasswordInvalida {
    // preparacion
    String email = "noexiste@test.com";
    String password = "Password123";
    when(this.repositorioUsuarioMock.buscarUsuario(email)).thenReturn(null);

    // validacion
    assertThrows(
      PasswordInvalida.class,
      () -> this.servicioLogin.consultarUsuario(email, password)
    );
  }

  @Test
  public void consultarUsuarioInactivoDeberiaLanzarExcepcion() {
    // preparacion
    String email = "test@test.com";
    String password = "password";
    Usuario usuarioEsperado = new Usuario();
    usuarioEsperado.setPassword(password);
    usuarioEsperado.setActivo(false);
    when(this.repositorioUsuarioMock.buscarUsuario(email)).thenReturn(usuarioEsperado);
    when(this.passwordEncoderMock.matches(anyString(), anyString())).thenReturn(true);
    // ejecucion y validacion
    assertThrows(UsuarioInactivo.class, () -> this.servicioLogin.consultarUsuario(email, password));
  }

  @Test
  public void registrarUsuarioSiNoExisteDeberiaGuardarlo() throws Exception {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setEmail("nuevo@test.com");
    usuario.setPassword("Contra1");
    when(this.repositorioUsuarioMock.buscar(usuario.getEmail())).thenReturn(null);

    // ejecucion
    this.servicioLogin.registrar(usuario);

    // validacion
    assertThat(usuario.getActivo(), is(false));
    verify(this.repositorioUsuarioMock, times(1)).guardar(usuario);
  }

  @Test
  public void registrarUsuarioSiExisteDeberiaLanzarExcepcion() {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setEmail("existe@test.com");
    usuario.setPassword("Contra1");
    when(this.repositorioUsuarioMock.buscar(usuario.getEmail())).thenReturn(new Usuario());

    // ejecucion y validacion
    assertThrows(UsuarioExistente.class, () -> this.servicioLogin.registrar(usuario));
    verify(this.repositorioUsuarioMock, times(0)).guardar(usuario);
  }
}
