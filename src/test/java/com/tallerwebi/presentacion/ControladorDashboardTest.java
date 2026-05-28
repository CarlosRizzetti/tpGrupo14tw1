package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.*;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.dominio.services.ServicioTimer;
import com.tallerwebi.presentacion.controller.ControladorDashboard;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ResponseDTO;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

public class ControladorDashboardTest {

  private HttpSession sessionMock;
  private ServicioDashboard servicioDashboardMock;
  private ControladorDashboard controladorDashboard;
  private ServicioTimer servicioTimerMock;

  @BeforeEach
  public void init() {
    servicioDashboardMock = mock(ServicioDashboard.class);
    sessionMock = mock(HttpSession.class);
    this.servicioTimerMock = mock(ServicioTimer.class);
    controladorDashboard = new ControladorDashboard(servicioDashboardMock, servicioTimerMock);
  }

  private Timer buildTimerConCategoria(Long timerId, Long categoriaId) {
    Timer timer = new Timer();
    timer.setId(timerId);

    if (categoriaId != null) {
      Categoria categoria = new Categoria();
      categoria.setId(categoriaId);
      timer.setCategoria(categoria);
    }

    return timer;
  }

  @Test
  @DisplayName("HP-01 | importarTimer | Retorna 200 cuando el timer se importa correctamente")
  void importarTimer_deberiaRetornar200CuandoSeImportaCorrectamente() {
    Timer timer = buildTimerConCategoria(1L, 10L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);

    ResponseEntity<ResponseDTO> respuesta = controladorDashboard.importarTimer(
      1L,
      99L,
      sessionMock
    );

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertNotNull(respuesta.getBody());
    assertTrue(respuesta.getBody().isSuccess());
    assertEquals("Timer importado correctamente", respuesta.getBody().getMessage());
  }

  @Test
  @DisplayName(
    "HP-02 | importarTimer | Llama a servicioDashboard.importarTimer con los ids correctos"
  )
  void importarTimer_deberiaLlamarAlServicioConLosIdsCorrectos() {
    Timer timer = buildTimerConCategoria(1L, 10L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);

    controladorDashboard.importarTimer(1L, 99L, sessionMock);

    verify(servicioDashboardMock).importarTimer(1L, 99L);
  }

  @Test
  @DisplayName("NEG-01 | importarTimer | Retorna 400 cuando el timerId es inválido")
  void importarTimer_deberiaRetornar400CuandoElTimerIdEsInvalido() {
    ResponseEntity<ResponseDTO> respuesta = controladorDashboard.importarTimer(
      -1L,
      99L,
      sessionMock
    );

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    assertFalse(respuesta.getBody().isSuccess());
  }

  @Test
  @DisplayName("NEG-02 | importarTimer | Retorna 400 cuando el categoryId es inválido")
  void importarTimer_deberiaRetornar400CuandoElCategoryIdEsInvalido() {
    ResponseEntity<ResponseDTO> respuesta = controladorDashboard.importarTimer(
      1L,
      -1L,
      sessionMock
    );

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    assertFalse(respuesta.getBody().isSuccess());
  }

  @Test
  @DisplayName("NEG-03 | importarTimer | Retorna 400 cuando el timer no existe")
  void importarTimer_deberiaRetornar400CuandoElTimerNoExiste() {
    when(servicioTimerMock.buscarPorId(1L)).thenReturn(null);

    ResponseEntity<ResponseDTO> respuesta = controladorDashboard.importarTimer(
      1L,
      99L,
      sessionMock
    );

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    assertFalse(respuesta.getBody().isSuccess());
  }

  @Test
  @DisplayName("NEG-04 | importarTimer | Retorna 400 cuando el timer ya pertenece a la categoria")
  void importarTimer_deberiaRetornar400CuandoElTimerYaPerteneceALaCategoria() {
    Timer timer = buildTimerConCategoria(1L, 99L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);

    ResponseEntity<ResponseDTO> respuesta = controladorDashboard.importarTimer(
      1L,
      99L,
      sessionMock
    );

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    assertFalse(respuesta.getBody().isSuccess());
    assertEquals("El timer ya pertenece a esta categoría", respuesta.getBody().getMessage());
  }

  @Test
  @DisplayName("NEG-05 | importarTimer | Retorna 400 cuando la categoria del timer es null")
  void importarTimer_deberiaRetornar400CuandoLaCategoriaDelTimerEsNull() {
    Timer timer = buildTimerConCategoria(1L, null);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);

    ResponseEntity<ResponseDTO> respuesta = controladorDashboard.importarTimer(
      1L,
      99L,
      sessionMock
    );

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    assertFalse(respuesta.getBody().isSuccess());
  }

  @Test
  @DisplayName("NEG-06 | importarTimer | Retorna 500 cuando el servicio falla inesperadamente")
  void importarTimer_deberiaRetornar500CuandoElServicioFallaInesperadamente() {
    Timer timer = buildTimerConCategoria(1L, 10L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    doThrow(new RuntimeException("Error inesperado"))
      .when(servicioDashboardMock)
      .importarTimer(1L, 99L);

    ResponseEntity<ResponseDTO> respuesta = controladorDashboard.importarTimer(
      1L,
      99L,
      sessionMock
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
    assertFalse(respuesta.getBody().isSuccess());
    assertEquals("Error al importar el timer", respuesta.getBody().getMessage());
  }

  @Test
  @DisplayName("SEC-01 | importarTimer | No expone información interna en errores 500")
  void importarTimer_noDeberiaExponerInformacionInternaEnErrores500() {
    Timer timer = buildTimerConCategoria(1L, 10L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    doThrow(new RuntimeException("password=1234 datos sensibles"))
      .when(servicioDashboardMock)
      .importarTimer(1L, 99L);

    ResponseEntity<ResponseDTO> respuesta = controladorDashboard.importarTimer(
      1L,
      99L,
      sessionMock
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
    assertEquals("Error al importar el timer", respuesta.getBody().getMessage());
  }

  @Test
  public void queSeEnvienCorrectamenteLosTimersALaVistaCuandoNoSonNull() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    String fechaCreacionISo = fechaCreacion.toString();
    String fechaVencimientoISO = fechaCreacion.plusDays(3).toString();
    Categoria categoria = new Categoria("mccafe.png", true, "mccafe");
    categoria.setId(1L);
    CategoriaDto categoriaDTO = new CategoriaDto(categoria);
    String nombre = "hamburguesa";
    String ubicacion = "horno";
    TimerDTO timer = new TimerDTO(
      1L,
      nombre,
      "1AF34",
      fechaCreacionISo,
      fechaVencimientoISO,
      ubicacion
    );
    List<TimerDTO> timersActivos = List.of(timer);
    when(servicioDashboardMock.obtenerTimersActivos(anyLong())).thenReturn(timersActivos);
    when(sessionMock.getAttribute(anyString())).thenReturn(categoriaDTO);

    ModelAndView mav = controladorDashboard.index(sessionMock);
    Map<String, Object> model = mav.getModel();

    assertEquals(2, model.size());
    assertTrue(model.containsKey("timers"));
    List<TimerDTO> timers = (List<TimerDTO>) model.get("timers");
    assertEquals(1, timers.size());
    assertEquals(1L, timers.get(0).getId());
    assertEquals(timer, timers.get(0));
  }

  @Test
  public void queAlEliminarUnTimerCorrectamenteRetorneHttpStatusOkYMensajeExito() {
    Long timerId = 1L;
    Long categoryId = 2L;

    doNothing().when(servicioDashboardMock).eliminarTimer(timerId);

    ResponseEntity<String> respuesta = controladorDashboard.eliminarTimer(timerId, categoryId);

    verify(servicioDashboardMock, times(1)).eliminarTimer(timerId);
    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertEquals("Timer eliminado correctamente", respuesta.getBody());
  }

  @Test
  public void queAlEliminarUnTimerConIdInvalidoLanceIllegalArgumentExceptionYRetorneBadRequest() {
    Long timerId = 99L;
    Long categoryId = 2L;

    doThrow(new IllegalArgumentException("Timer no encontrado"))
      .when(servicioDashboardMock)
      .eliminarTimer(timerId);

    ResponseEntity<String> respuesta = controladorDashboard.eliminarTimer(timerId, categoryId);

    verify(servicioDashboardMock, times(1)).eliminarTimer(timerId);
    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    assertEquals("Timer no encontrado", respuesta.getBody());
  }

  @Test
  public void queAlEliminarUnTimerYSiOcurreUnErrorInesperadoRetorneInternalServerError() {
    Long timerId = 1L;
    Long categoryId = 2L;

    doThrow(new RuntimeException("Error de base de datos"))
      .when(servicioDashboardMock)
      .eliminarTimer(timerId);

    ResponseEntity<String> respuesta = controladorDashboard.eliminarTimer(timerId, categoryId);

    verify(servicioDashboardMock, times(1)).eliminarTimer(timerId);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
    assertEquals("Error al eliminar el timer", respuesta.getBody());
  }
}
