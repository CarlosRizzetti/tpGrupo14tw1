package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.presentacion.controller.ControladorDashboardGlobal;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorDashboardGlobalTest {

  private ServicioCategoria servicioCategoriaMock;
  private ServicioDashboard servicioDashboardMock;
  private ControladorDashboardGlobal controlador;

  @BeforeEach
  public void init() {
    servicioCategoriaMock = mock(ServicioCategoria.class);
    servicioDashboardMock = mock(ServicioDashboard.class);
    controlador = new ControladorDashboardGlobal(servicioCategoriaMock, servicioDashboardMock);
  }

  @Test
  @DisplayName("HP-G1 | dashboardGlobal | Devuelve error cuando no hay categorias")
  void dashboardGlobal_deberiaMostrarErrorCuandoNoHayCategorias() {
    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu()).thenReturn(List.of());

    ModelAndView mav = controlador.dashboardGlobal();

    Map<String, Object> model = mav.getModel();
    assertTrue(model.containsKey("error"));
    assertEquals("No hay categorías disponibles", model.get("error"));
  }

  @Test
  @DisplayName("HP-G2 | dashboardGlobal | Muestra mensaje cuando no hay timers activos")
  void dashboardGlobal_deberiaMostrarMensajeCuandoNoHayTimersActivos() {
    CategoriaDto c1 = new CategoriaDto();
    c1.setId(1L);
    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu()).thenReturn(List.of(c1));
    when(servicioDashboardMock.obtenerTimersActivos(anyLong())).thenReturn(List.of());

    ModelAndView mav = controlador.dashboardGlobal();
    Map<String, Object> model = mav.getModel();

    assertTrue(model.containsKey("error"));
    assertEquals("No hay timers activos", model.get("error"));
    assertTrue(model.containsKey("categorias"));
  }

  @Test
  @DisplayName(
    "HP-G3 | dashboardGlobal | Devuelve categorias y timersPorCategoria cuando hay timers"
  )
  void dashboardGlobal_deberiaIncluirTimersPorCategoriaCuandoExisten() {
    CategoriaDto c1 = new CategoriaDto();
    c1.setId(1L);
    CategoriaDto c2 = new CategoriaDto();
    c2.setId(2L);

    TimerDTO t1 = new TimerDTO();
    t1.setId(10L);

    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu()).thenReturn(List.of(c1, c2));
    when(servicioDashboardMock.obtenerTimersActivos(1L)).thenReturn(List.of(t1));
    when(servicioDashboardMock.obtenerTimersActivos(2L)).thenReturn(List.of());

    ModelAndView mav = controlador.dashboardGlobal();
    Map<String, Object> model = mav.getModel();

    assertTrue(model.containsKey("categorias"));
    assertTrue(model.containsKey("timersPorCategoria"));

    @SuppressWarnings("unchecked")
    var timersMap = (java.util.Map<Long, List<TimerDTO>>) model.get("timersPorCategoria");
    assertTrue(timersMap.containsKey(1L));
    assertEquals(1, timersMap.get(1L).size());
    assertEquals(10L, timersMap.get(1L).get(0).getId());
  }

  @Test
  @DisplayName(
    "NEG-G1 | dashboardGlobal | Maneja excepciones devolviendo mensaje de error genérico"
  )
  void dashboardGlobal_deberiaManejarExcepciones() {
    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu())
      .thenThrow(new RuntimeException("boom"));

    ModelAndView mav = controlador.dashboardGlobal();
    Map<String, Object> model = mav.getModel();

    assertTrue(model.containsKey("error"));
    assertEquals("Error al cargar el dashboard global", model.get("error"));
  }
}
