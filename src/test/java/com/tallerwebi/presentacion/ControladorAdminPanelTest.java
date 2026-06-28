package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.presentacion.controller.ControladorAdminPanel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.ModelAndView;

public class ControladorAdminPanelTest {

  private ControladorAdminPanel controlador;
  private Authentication authenticationMock;

  @BeforeEach
  public void init() {
    controlador = new ControladorAdminPanel();
    authenticationMock = mock(Authentication.class);
  }

  @Test
  public void queSiAuthenticationEsNullRedirijaAAccesoDenegado() {
    ModelAndView mav = controlador.panelDeControl(null);

    assertEquals("redirect:/acceso-denegado", mav.getViewName());
  }

  @Test
  public void queSiElUsuarioNoEstaAutenticadoRedirijaAAccesoDenegado() {
    doReturn(false).when(authenticationMock).isAuthenticated();

    ModelAndView mav = controlador.panelDeControl(authenticationMock);

    assertEquals("redirect:/acceso-denegado", mav.getViewName());
  }

  @Test
  public void queSiElUsuarioNoEsAdminRedirijaAAccesoDenegado() {
    doReturn(true).when(authenticationMock).isAuthenticated();

    doAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_USER")))
      .when(authenticationMock)
      .getAuthorities();

    ModelAndView mav = controlador.panelDeControl(authenticationMock);

    assertEquals("redirect:/acceso-denegado", mav.getViewName());
  }

  @Test
  public void queSiElUsuarioEsAdminMuestreElPanel() {
    doReturn(true).when(authenticationMock).isAuthenticated();

    doReturn("admin@test.com").when(authenticationMock).getName();

    doAnswer(invocation -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
      .when(authenticationMock)
      .getAuthorities();

    ModelAndView mav = controlador.panelDeControl(authenticationMock);

    assertEquals("funcionalidadesAdmin/panel", mav.getViewName());
    assertEquals("admin@test.com", mav.getModel().get("email"));
  }

  @Test
  public void queAccesoDenegadoRetorneLaVistaCorrecta() {
    ModelAndView mav = controlador.accesoDenegado();

    assertEquals("acceso-denegado", mav.getViewName());
  }
}
