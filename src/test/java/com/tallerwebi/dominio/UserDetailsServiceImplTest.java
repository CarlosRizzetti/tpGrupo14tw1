package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.EstadoUsuario;
import com.tallerwebi.dominio.excepcion.UsuarioInactivo;
import com.tallerwebi.dominio.excepcion.UsuarioSinCategorias;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.services.UserDetailsServiceImpl;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImplTest {

  private RepositorioUsuario repositorioUsuario;
  private UserDetailsServiceImpl userDetailsService;

  @BeforeEach
  public void init() {
    repositorioUsuario = mock(RepositorioUsuario.class);
    userDetailsService = new UserDetailsServiceImpl(repositorioUsuario);
  }

  @Test
  public void siElUsuarioNoExisteDebeLanzarUsernameNotFoundException() {
    when(repositorioUsuario.buscar("test@test.com")).thenReturn(null);
    assertThrows(
      UsernameNotFoundException.class,
      () -> userDetailsService.loadUserByUsername("test@test.com")
    );
  }

  @Test
  public void siElUsuarioEstaInactivoDebeLanzarUsuarioInactivo() {
    Usuario usuario = new Usuario();
    usuario.setEstado(EstadoUsuario.PENDIENTE);
    when(repositorioUsuario.buscar("test@test.com")).thenReturn(usuario);

    assertThrows(
      UsuarioInactivo.class,
      () -> userDetailsService.loadUserByUsername("test@test.com")
    );
  }

  @Test
  public void siElUsuarioNoEsAdminYNoTieneCategoriasDebeLanzarUsuarioSinCategorias() {
    Usuario usuario = new Usuario();
    usuario.setEstado(EstadoUsuario.ACTIVO);
    usuario.setRol("USER");
    usuario.setCategorias(new HashSet<>());
    when(repositorioUsuario.buscar("test@test.com")).thenReturn(usuario);

    assertThrows(
      UsuarioSinCategorias.class,
      () -> userDetailsService.loadUserByUsername("test@test.com")
    );
  }

  @Test
  public void siEsAdminSinCategoriasDebeDevolverUserDetails() {
    Usuario usuario = new Usuario();
    usuario.setEmail("admin@test.com");
    usuario.setPassword("1234");
    usuario.setEstado(EstadoUsuario.ACTIVO);
    usuario.setRol("ADMIN");
    usuario.setCategorias(new HashSet<>());
    when(repositorioUsuario.buscar("admin@test.com")).thenReturn(usuario);

    UserDetails userDetails = userDetailsService.loadUserByUsername("admin@test.com");

    assertNotNull(userDetails);
    assertEquals("admin@test.com", userDetails.getUsername());
  }

  @Test
  public void siNoEsAdminPeroTieneCategoriasDebeDevolverUserDetails() {
    Usuario usuario = new Usuario();
    usuario.setEmail("user@test.com");
    usuario.setPassword("1234");
    usuario.setEstado(EstadoUsuario.ACTIVO);
    usuario.setRol("USER");
    usuario.setCategorias(new HashSet<>());
    usuario.getCategorias().add(new Categoria());
    when(repositorioUsuario.buscar("user@test.com")).thenReturn(usuario);

    UserDetails userDetails = userDetailsService.loadUserByUsername("user@test.com");

    assertNotNull(userDetails);
    assertEquals("user@test.com", userDetails.getUsername());
  }
}
