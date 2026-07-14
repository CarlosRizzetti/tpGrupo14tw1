package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.presentacion.controller.ControladorTimer;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

class ControladorTimerTest {

  private ServicioTimer servicioTimerMock;

  private ServicioCategoria servicioCategoriaMock;

  private ControladorTimer controladorTimer;
  private ServicioLote servicioLoteMock;

  @BeforeEach
  public void setUp() {
    this.servicioTimerMock = mock(ServicioTimer.class);
    this.servicioCategoriaMock = mock(ServicioCategoria.class);
    this.servicioLoteMock = mock(ServicioLote.class);
    this.controladorTimer =
      new ControladorTimer(servicioTimerMock, servicioCategoriaMock, servicioLoteMock);
  }

  @Test
  @DisplayName(
    "HP-01 | verHistorialDeTimers | Retorna ModelAndView con vista historialTimers y modelos cargados"
  )
  void verHistorialDeTimers_deberiaRetornarVistaHistorialYModelos() {
    // Arrange: Preparar datos
    TimerDTO timerMock = new TimerDTO();
    CategoriaDto categoriaMock = new CategoriaDto();
    categoriaMock.setId(1L);
    categoriaMock.setNombre("Categoria Test");

    List<TimerDTO> timers = List.of(timerMock);
    List<CategoriaDto> categorias = List.of(categoriaMock);

    // Arrange: Comportamiento de los mocks
    when(servicioTimerMock.obtenerTodosLosTimers()).thenReturn(timers);
    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu()).thenReturn(categorias);

    // Act: Ejecutar el método
    ModelAndView respuesta = controladorTimer.verHistorialDeTimers();

    // Assert: Validar resultados
    assertNotNull(respuesta);
    assertEquals("funcionalidadesAdmin/timer/historialTimers", respuesta.getViewName());

    assertNotNull(respuesta.getModel());
    assertTrue(respuesta.getModel().containsKey("timers"));
    assertTrue(respuesta.getModel().containsKey("categorias"));

    assertEquals(timers, respuesta.getModel().get("timers"));
    assertEquals(categorias, respuesta.getModel().get("categorias"));
  }

  @Test
  @DisplayName(
    "HP-02 | obtenerTimersConFiltro | Retorna 200 con la lista de timers filtrados en un Map"
  )
  void obtenerTimersConFiltro_deberiaRetornar200ConListaDeTimers() {
    // Arrange: Preparar datos
    EstadoTimer estado = EstadoTimer.ACTIVO; // Asumiendo que es un Enum
    Long categoriaId = 5L;
    TimerDTO timerMock = new TimerDTO();

    List<TimerDTO> timersEsperados = List.of(timerMock);

    // Arrange: Comportamiento del mock
    when(servicioTimerMock.obtenerTimersConFiltro(estado, categoriaId)).thenReturn(timersEsperados);

    // Act: Ejecutar el método
    ResponseEntity<?> respuesta = controladorTimer.obtenerTimersConFiltro(estado, categoriaId);

    // Assert: Validar resultados
    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertNotNull(respuesta.getBody());

    // Cast seguro para validar el contenido del mapa retornado
    @SuppressWarnings("unchecked")
    Map<String, Object> cuerpoRespuesta = (Map<String, Object>) respuesta.getBody();

    assertTrue(cuerpoRespuesta.containsKey("timers"));

    @SuppressWarnings("unchecked")
    List<TimerDTO> timersRetornados = (List<TimerDTO>) cuerpoRespuesta.get("timers");

    assertEquals(1, timersRetornados.size());
    assertEquals(timersEsperados, timersRetornados);
  }
}
