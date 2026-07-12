package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioLote;
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
  private ServicioLote servicioLoteMock;

  @BeforeEach
  public void init() {
    servicioLoteMock = mock(ServicioLote.class);
    controlador = new ControladorAdminPanel(servicioLoteMock);
    authenticationMock = mock(Authentication.class);
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
    when(servicioLoteMock.obtenerNotificacionesVencimiento()).thenReturn(notificacionesMock);

    ModelAndView mav = controlador.panelDeControl(authenticationMock);

    assertEquals("funcionalidadesAdmin/panel", mav.getViewName());
    assertEquals("admin@test.com", mav.getModel().get("email"));
    assertEquals(notificacionesMock, mav.getModel().get("notificaciones"));
  }
}
