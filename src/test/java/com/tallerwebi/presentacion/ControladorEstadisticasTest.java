package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioEstadistica;
import com.tallerwebi.presentacion.controller.ControladorEstadisticas;
import com.tallerwebi.presentacion.dto.EstadisticasDTO;
import com.tallerwebi.presentacion.dto.PuntoEstadisticoDTO;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
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

  @Test
  public void queSePuedaExportarExcelConDatosValidos() throws Exception {
    // 1. Preparación (Given)
    int dias = 30;
    EstadisticasDTO estadisticasMock = new EstadisticasDTO();

    // Creamos un punto falso para que el test entre al bucle 'for' y cubra esas líneas
    List<PuntoEstadisticoDTO> listaMock = new ArrayList<>();
    PuntoEstadisticoDTO punto = new PuntoEstadisticoDTO();
    punto.setEtiqueta("Prueba");
    punto.setValor(10L);
    listaMock.add(punto);

    // Llenamos el DTO para evitar NullPointerExceptions en el controlador
    estadisticasMock.setProductosMasUtilizados(listaMock);
    estadisticasMock.setVencimientosPorEstado(listaMock);
    estadisticasMock.setVencimientosPorDia(listaMock);
    estadisticasMock.setModificacionesStockPorDia(listaMock);
    estadisticasMock.setDemandaPorDiaSemana(listaMock);
    estadisticasMock.setDemandaPorHora(listaMock);

    // Mockeamos el servicio y los objetos HTTP
    when(servicioEstadisticaMock.obtenerEstadisticas(dias)).thenReturn(estadisticasMock);
    HttpServletResponse responseMock = mock(HttpServletResponse.class);
    ServletOutputStream outputStreamMock = mock(ServletOutputStream.class);
    when(responseMock.getOutputStream()).thenReturn(outputStreamMock);

    // 2. Ejecución (When)
    controlador.exportarExcel(dias, responseMock);

    // 3. Verificación (Then)
    // Verificamos que se hayan seteado las cabeceras correctas de Excel
    verify(responseMock)
      .setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    verify(responseMock).setHeader(eq("Content-Disposition"), anyString());
    // Verificamos que el archivo se haya escrito en la respuesta
    verify(responseMock.getOutputStream(), atLeastOnce())
      .write(any(byte[].class), anyInt(), anyInt()); // El try-with-resources cierra el flujo
  }

  @Test
  public void queAlExportarExcelConDiasInvalidosDevuelvaBadRequest() throws Exception {
    // Preparación
    int diasInvalidos = -5;
    when(servicioEstadisticaMock.obtenerEstadisticas(diasInvalidos))
      .thenThrow(new IllegalArgumentException("Los días no pueden ser negativos"));
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Ejecución
    controlador.exportarExcel(diasInvalidos, responseMock);

    // Verificación
    // Como lanzamos IllegalArgumentException, el controlador debe capturarla y devolver un 400
    verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void queAlExportarExcelConErrorInternoDevuelvaInternalServerError() throws Exception {
    // Preparación
    int dias = 30;
    when(servicioEstadisticaMock.obtenerEstadisticas(dias))
      .thenThrow(new RuntimeException("Error simulado de Base de Datos"));
    HttpServletResponse responseMock = mock(HttpServletResponse.class);

    // Ejecución
    controlador.exportarExcel(dias, responseMock);

    // Verificación
    // Ante un error grave, el controlador debe devolver un 500
    verify(responseMock).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }
}
