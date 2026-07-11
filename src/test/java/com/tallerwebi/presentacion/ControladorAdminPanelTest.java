package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import com.tallerwebi.presentacion.controller.ControladorAdminPanel;
import com.tallerwebi.presentacion.dto.NotificacionVencimientoDto;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.ModelAndView;

public class ControladorAdminPanelTest {

  private ControladorAdminPanel controlador;
  private Authentication authenticationMock;
  private ServicioArticulo servicioArticuloMock;

  @BeforeEach
  public void init() {
    servicioArticuloMock = mock(ServicioArticulo.class);
    controlador = new ControladorAdminPanel(servicioArticuloMock);
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

    List<NotificacionVencimientoDto> notificacionesMock = Collections.singletonList(
      new NotificacionVencimientoDto()
    );
    when(servicioArticuloMock.obtenerNotificacionesVencimiento()).thenReturn(notificacionesMock);

    ModelAndView mav = controlador.panelDeControl(authenticationMock);

    assertEquals("funcionalidadesAdmin/panel", mav.getViewName());
    assertEquals("admin@test.com", mav.getModel().get("email"));
    assertEquals(notificacionesMock, mav.getModel().get("notificaciones"));
  }

  @Test
  public void queAccesoDenegadoRetorneLaVistaCorrecta() {
    ModelAndView mav = controlador.accesoDenegado();

    assertEquals("acceso-denegado", mav.getViewName());
  }
}
