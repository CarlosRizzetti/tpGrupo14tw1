package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioEstadistica;
import com.tallerwebi.presentacion.controller.ControladorEstadisticas;
import com.tallerwebi.presentacion.dto.EstadisticasDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

public class ControladorEstadisticasTest {

  private ServicioEstadistica servicioEstadisticaMock;
  private ControladorEstadisticas controlador;

  @BeforeEach
  public void init() {
    servicioEstadisticaMock = mock(ServicioEstadistica.class);
    controlador = new ControladorEstadisticas(servicioEstadisticaMock);
  }

  @Test
  @DisplayName("HP-01 | index | Devuelve la vista de estadísticas con el rango por defecto")
  public void indexDeberiaDevolverLaVistaConDiasPorDefecto() {
    ModelAndView mav = controlador.index();

    assertEquals("estadisticas/estadisticas", mav.getViewName());
    assertEquals(30, mav.getModel().get("dias"));
  }

  @Test
  @DisplayName("HP-02 | obtenerDatos | Devuelve 200 con las estadísticas del servicio")
  public void obtenerDatosDeberiaDevolverOkConLasEstadisticas() {
    EstadisticasDTO estadisticas = EstadisticasDTO.builder().build();
    when(servicioEstadisticaMock.obtenerEstadisticas(30)).thenReturn(estadisticas);

    ResponseEntity<EstadisticasDTO> respuesta = controlador.obtenerDatos(30);

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertSame(estadisticas, respuesta.getBody());
    verify(servicioEstadisticaMock, times(1)).obtenerEstadisticas(30);
  }

  @Test
  @DisplayName("NEG-01 | obtenerDatos | Devuelve 400 cuando el servicio rechaza el parámetro")
  public void obtenerDatosDeberiaDevolverBadRequestConParametroInvalido() {
    when(servicioEstadisticaMock.obtenerEstadisticas(anyInt()))
      .thenThrow(new IllegalArgumentException("inválido"));

    ResponseEntity<EstadisticasDTO> respuesta = controlador.obtenerDatos(0);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
  }

  @Test
  @DisplayName("NEG-02 | obtenerDatos | Devuelve 500 ante un error inesperado del servicio")
  public void obtenerDatosDeberiaDevolverErrorInternoAnteExcepcion() {
    when(servicioEstadisticaMock.obtenerEstadisticas(anyInt()))
      .thenThrow(new RuntimeException("boom"));

    ResponseEntity<EstadisticasDTO> respuesta = controlador.obtenerDatos(30);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
  }
}
