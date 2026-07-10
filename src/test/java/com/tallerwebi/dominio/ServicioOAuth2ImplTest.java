package com.tallerwebi.dominio;

import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.EstadoUsuario;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.services.ServicioOAuth2Impl;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioOAuth2ImplTest {

  private RepositorioUsuario repositorioUsuario;
  private ServicioOAuth2Impl servicioOAuth2;
  private HttpServletResponse response;

  @BeforeEach
  public void setUp() {
    repositorioUsuario = mock(RepositorioUsuario.class);
    response = mock(HttpServletResponse.class);
    servicioOAuth2 = new ServicioOAuth2Impl(repositorioUsuario);
  }

  @Test
  public void queGuardeUnUsuarioSiNoExisteYRedirijaPorEstarPendiente() throws IOException {
    when(repositorioUsuario.buscar("test@google.com")).thenReturn(null);

    servicioOAuth2.procesarUsuarioGoogle("test@google.com", "Test User", response);

    verify(repositorioUsuario, times(1)).guardar(any(Usuario.class));
    verify(response, times(1)).sendRedirect("/login-oauth?pendiente=true");
  }

  @Test
  public void queRedirijaAPendienteSiUsuarioExistePeroNoEstaActivo() throws IOException {
    Usuario usuario = new Usuario();
    usuario.setEstado(EstadoUsuario.PENDIENTE);
    when(repositorioUsuario.buscar("test@google.com")).thenReturn(usuario);

    servicioOAuth2.procesarUsuarioGoogle("test@google.com", "Test User", response);

    verify(repositorioUsuario, never()).guardar(any(Usuario.class));
    verify(response, times(1)).sendRedirect("/login-oauth?pendiente=true");
  }

  @Test
  public void queRedirijaAHomeSiUsuarioEstaActivoYEsAdmin() throws IOException {
    Usuario usuario = new Usuario();
    usuario.setEstado(EstadoUsuario.ACTIVO);
    usuario.setRol("ADMIN");
    when(repositorioUsuario.buscar("admin@google.com")).thenReturn(usuario);

    servicioOAuth2.procesarUsuarioGoogle("admin@google.com", "Admin User", response);

    verify(response, times(1)).sendRedirect("/home");
  }

  @Test
  public void queRedirijaASinCategoriasSiUsuarioEstaActivoEsUSERYNoTieneCategorias()
    throws IOException {
    Usuario usuario = new Usuario();
    usuario.setEstado(EstadoUsuario.ACTIVO);
    usuario.setRol("USER");
    when(repositorioUsuario.buscar("user@google.com")).thenReturn(usuario);

    servicioOAuth2.procesarUsuarioGoogle("user@google.com", "User", response);

    verify(response, times(1)).sendRedirect("/login-oauth?sinCategorias=true");
    verify(response, times(1)).sendRedirect("/home");
  }

  @Test
  public void queRedirijaAHomeSiUsuarioEstaActivoEsUSERYTieneCategorias() throws IOException {
    Usuario usuario = new Usuario();
    usuario.setEstado(EstadoUsuario.ACTIVO);
    usuario.setRol("USER");
    usuario.setCategorias(new java.util.HashSet<>(java.util.Arrays.asList(new Categoria())));
    when(repositorioUsuario.buscar("user@google.com")).thenReturn(usuario);

    servicioOAuth2.procesarUsuarioGoogle("user@google.com", "User", response);

    verify(response, times(1)).sendRedirect("/home");
  }
}
