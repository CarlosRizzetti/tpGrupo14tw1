package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioValidacionIdentidad;
import com.tallerwebi.presentacion.controller.ControladorValidacionIdentidad;
import com.tallerwebi.presentacion.dto.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

public class ControladorValidacionIdentidadTest {

  private ControladorValidacionIdentidad controladorValidacionIdentidad;
  private ServicioValidacionIdentidad servicioValidacionIdentidadMock;

  @BeforeEach
  public void init() {
    servicioValidacionIdentidadMock = mock(ServicioValidacionIdentidad.class);
    controladorValidacionIdentidad =
      new ControladorValidacionIdentidad(servicioValidacionIdentidadMock);
  }

  @Test
  public void solicitarValidacionDeberiaRetornarMensajeGenerico() {
    ResponseEntity<ResponseDTO> response = controladorValidacionIdentidad.solicitarValidacion(
      "test@test.com"
    );

    assertThat(response.getStatusCodeValue(), is(200));
    assertThat(response.getBody().getMessage(), containsString("Si el correo existe"));
    verify(servicioValidacionIdentidadMock, times(1)).solicitarValidacion("test@test.com");
  }

  @Test
  public void validarIdentidadConTokenValidoDeberiaRetornarSuccess() {
    when(servicioValidacionIdentidadMock.validarToken("token")).thenReturn(true);

    ResponseEntity<ResponseDTO> response = controladorValidacionIdentidad.validarIdentidad("token");

    assertThat(response.getBody().isSuccess(), is(true));
    assertThat(response.getBody().getMessage(), containsString("Cuenta activada"));
  }

  @Test
  public void validarIdentidadConTokenInvalidoDeberiaRetornarError() {
    when(servicioValidacionIdentidadMock.validarToken("token")).thenReturn(false);

    ResponseEntity<ResponseDTO> response = controladorValidacionIdentidad.validarIdentidad("token");

    assertThat(response.getBody().isSuccess(), is(false));
    assertThat(response.getBody().getMessage(), containsString("Token inválido"));
  }

  @Test
  public void mostrarValidacionSinTokenDeberiaRetornarVista() {
    ModelAndView mav = controladorValidacionIdentidad.mostrarValidacion(null);

    assertThat(mav.getViewName(), equalToIgnoringCase("loginYRegistro/validacion-identidad"));
    assertThat(mav.getModel().get("message"), is(nullValue()));
  }

  @Test
  public void mostrarValidacionConTokenValidoDeberiaMostrarExitoYRedirect() {
    when(servicioValidacionIdentidadMock.validarToken("token")).thenReturn(true);

    ModelAndView mav = controladorValidacionIdentidad.mostrarValidacion("token");

    assertThat(mav.getViewName(), equalToIgnoringCase("loginYRegistro/validacion-identidad"));
    assertThat(mav.getModel().get("success"), is(true));
    assertThat(mav.getModel().get("message").toString(), containsString("Cuenta activada"));
    assertThat(mav.getModel().get("redirectUrl").toString(), equalToIgnoringCase("/login"));
    assertThat(mav.getModel().get("redirectDelayMs"), is(3000));
  }
}
