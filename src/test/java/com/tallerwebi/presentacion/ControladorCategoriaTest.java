package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.presentacion.controller.ControladorCategoria;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.ModelAndView;

public class ControladorCategoriaTest {

  private ControladorCategoria controlador;
  private ServicioCategoria servicioCategoriaMock;
  private Authentication authenticationMock;

  @BeforeEach
  public void init() {
    servicioCategoriaMock = mock(ServicioCategoria.class);
    authenticationMock = mock(Authentication.class);

    controlador = new ControladorCategoria(servicioCategoriaMock);
  }

  @Test
  public void queSinUsuarioAutenticadoRetorneLaVistaHomeConCategorias() {
    List<CategoriaDto> categorias = List.of(new CategoriaDto());

    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu()).thenReturn(categorias);

    ModelAndView mav = controlador.index(null);

    assertEquals("home", mav.getViewName());
    assertEquals(categorias, mav.getModel().get("categorias"));
    assertNull(mav.getModel().get("userEmail"));
    assertNull(mav.getModel().get("userRol"));
  }

  @Test
  public void queConUsuarioAutenticadoAgregueEmailYRolAlModelo() {
    List<CategoriaDto> categorias = List.of(new CategoriaDto());

    doReturn(categorias).when(servicioCategoriaMock).obtenerLasCategoriasParaElMenu();

    doReturn(true).when(authenticationMock).isAuthenticated();

    doReturn("admin@test.com").when(authenticationMock).getName();

    doAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
      .when(authenticationMock)
      .getAuthorities();

    ModelAndView mav = controlador.index(authenticationMock);

    assertEquals("home", mav.getViewName());
    assertEquals(categorias, mav.getModel().get("categorias"));
    assertEquals("admin@test.com", mav.getModel().get("userEmail"));
    assertEquals("ADMIN", mav.getModel().get("userRol"));
  }

  @Test
  public void queSiNoExisteUnRolConPrefijoRoleElRolSeaNull() {
    doReturn(Collections.emptyList()).when(servicioCategoriaMock).obtenerLasCategoriasParaElMenu();

    doReturn(true).when(authenticationMock).isAuthenticated();

    doReturn("usuario@test.com").when(authenticationMock).getName();

    doAnswer(invocation -> List.of(new SimpleGrantedAuthority("ADMIN")))
      .when(authenticationMock)
      .getAuthorities();

    ModelAndView mav = controlador.index(authenticationMock);

    assertEquals("usuario@test.com", mav.getModel().get("userEmail"));
    assertNull(mav.getModel().get("userRol"));
  }
}
