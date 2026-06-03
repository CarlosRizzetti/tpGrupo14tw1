package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.*;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
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
  private ControladorDashboard controladorDashboard;
  private ServicioTimer servicioTimerMock;
  private ServicioProducto servicioProductoMock;

  private Timer buildTimerConProducto(Long timerId, Long productoId) {
    Producto producto = new Producto();
    producto.setId(productoId);

    Timer timer = new Timer();
    timer.setId(timerId);
    timer.setProducto(producto);
    timer.setGroupId("group-uuid-test");
    return timer;
  }

  @BeforeEach
  public void init() {
    sessionMock = mock(HttpSession.class);
    this.servicioTimerMock = mock(ServicioTimer.class);
    this.servicioProductoMock = mock(ServicioProducto.class);
    controladorDashboard = new ControladorDashboard(servicioTimerMock, servicioProductoMock);
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

  private List<CategoriaDto> buildCategorias() {
    CategoriaDto cat1 = new CategoriaDto();
    cat1.setId(1L);
    cat1.setNombre("Categoria A");

    CategoriaDto cat2 = new CategoriaDto();
    cat2.setId(2L);
    cat2.setNombre("Categoria B");

    return List.of(cat1, cat2);
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

    verify(servicioTimerMock).importarTimer(1L, 99L);
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
    when(servicioTimerMock.importarTimer(1L, 99L))
      .thenThrow(new ValidacionException("timer no encontrado"));

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
    when(servicioTimerMock.importarTimer(1L, 99L))
      .thenThrow(new ValidacionException("El timer ya pertenece a esta categoría"));

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
    when(servicioTimerMock.importarTimer(1L, 99L))
      .thenThrow(new ValidacionException("categoria del timer no encontrada"));

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
      .when(servicioTimerMock)
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
      .when(servicioTimerMock)
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
    when(servicioTimerMock.obtenerTimersActivos(anyLong())).thenReturn(timersActivos);
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

    doNothing().when(servicioTimerMock).modificarEstado(timerId, EstadoTimer.ELIMINADO);

    ResponseEntity<String> respuesta = controladorDashboard.eliminarTimer(timerId);

    verify(servicioTimerMock, times(1)).modificarEstado(timerId, EstadoTimer.ELIMINADO);
    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertEquals("Timer eliminado correctamente", respuesta.getBody());
  }

  @Test
  public void queAlEliminarUnTimerConIdInvalidoLanceIllegalArgumentExceptionYRetorneBadRequest() {
    Long timerId = 99L;

    doThrow(new IllegalArgumentException("Timer no encontrado"))
      .when(servicioTimerMock)
      .modificarEstado(timerId, EstadoTimer.ELIMINADO);

    ResponseEntity<String> respuesta = controladorDashboard.eliminarTimer(timerId);

    verify(servicioTimerMock, times(1)).modificarEstado(timerId, EstadoTimer.ELIMINADO);
    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    assertEquals("Timer no encontrado", respuesta.getBody());
  }

  @Test
  public void queAlEliminarUnTimerYSiOcurreUnErrorInesperadoRetorneInternalServerError() {
    Long timerId = 1L;

    doThrow(new RuntimeException("Error de base de datos"))
      .when(servicioTimerMock)
      .modificarEstado(timerId, EstadoTimer.ELIMINADO);

    ResponseEntity<String> respuesta = controladorDashboard.eliminarTimer(timerId);

    verify(servicioTimerMock, times(1)).modificarEstado(timerId, EstadoTimer.ELIMINADO);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
    assertEquals("Error al eliminar el timer", respuesta.getBody());
  }

  @Test
  @DisplayName(
    "HP-01 | obtenerCategoriasDeUnProducto | Retorna 200 con lista de categorias cuando el producto existe"
  )
  void obtenerCategorias_deberiaRetornar200ConCategoriasDelProducto() {
    Timer timer = buildTimerConProducto(1L, 10L);
    List<CategoriaDto> categorias = buildCategorias();

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioProductoMock.obtenerCategoriasDeUnProductoDisponiblesParaImportar(
        eq(10L),
        anyString()
      )
    )
      .thenReturn(categorias);

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertNotNull(respuesta.getBody());
    assertTrue(respuesta.getBody().containsKey("categorias"));
    assertEquals(categorias, respuesta.getBody().get("categorias"));
  }

  @Test
  @DisplayName("HP-02 | obtenerCategoriasDeUnProducto | Retorna 200 con una sola categoria")
  void obtenerCategorias_deberiaRetornar200ConUnaSolaCategoria() {
    Timer timer = buildTimerConProducto(1L, 10L);

    CategoriaDto categoria = new CategoriaDto();
    categoria.setId(1L);
    categoria.setNombre("Categoria Unica");

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioProductoMock.obtenerCategoriasDeUnProductoDisponiblesParaImportar(
        10L,
        "group-uuid-test"
      )
    )
      .thenReturn(List.of(categoria));

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertNotNull(respuesta.getBody());

    List<CategoriaDto> categoriasRetornadas = (List<CategoriaDto>) respuesta
      .getBody()
      .get("categorias");
    assertEquals(1, categoriasRetornadas.size());
  }

  @Test
  @DisplayName(
    "HP-03 | obtenerCategoriasDeUnProducto | La respuesta contiene exactamente las categorias del producto"
  )
  void obtenerCategorias_deberiaContenerExactamenteLasCategoriasDelProducto() {
    Timer timer = buildTimerConProducto(1L, 10L);
    List<CategoriaDto> categorias = buildCategorias();

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioProductoMock.obtenerCategoriasDeUnProductoDisponiblesParaImportar(
        10L,
        "group-uuid-test"
      )
    )
      .thenReturn(categorias);

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    List<CategoriaDto> categoriasRetornadas = (List<CategoriaDto>) respuesta
      .getBody()
      .get("categorias");

    assertEquals(2, categoriasRetornadas.size());
    assertEquals(1L, categoriasRetornadas.get(0).getId());
    assertEquals(2L, categoriasRetornadas.get(1).getId());
  }

  // =========================================================
  // NEGATIVE
  // =========================================================

  @Test
  @DisplayName("NEG-01 | obtenerCategoriasDeUnProducto | Retorna 400 cuando el ID es nulo")
  void obtenerCategorias_deberiaRetornar400CuandoIdEsNulo() {
    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(null);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName("NEG-02 | obtenerCategoriasDeUnProducto | Retorna 400 cuando el ID es cero")
  void obtenerCategorias_deberiaRetornar400CuandoIdEsCero() {
    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(0L);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName("NEG-03 | obtenerCategoriasDeUnProducto | Retorna 400 cuando el ID es negativo")
  void obtenerCategorias_deberiaRetornar400CuandoIdEsNegativo() {
    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(-1L);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName("NEG-04 | obtenerCategoriasDeUnProducto | Retorna 404 cuando el timer no existe")
  void obtenerCategorias_deberiaRetornar404CuandoTimerNoExiste() {
    when(servicioTimerMock.buscarPorId(99L)).thenReturn(null);

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(99L);

    assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName(
    "NEG-05 | obtenerCategoriasDeUnProducto | Retorna 404 cuando el producto del timer es nulo"
  )
  void obtenerCategorias_deberiaRetornar404CuandoProductoDelTimerEsNulo() {
    Timer timer = new Timer();
    timer.setId(1L);
    timer.setProducto(null); // sin producto

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName(
    "NEG-06 | obtenerCategoriasDeUnProducto | Retorna 500 cuando el servicio falla inesperadamente"
  )
  void obtenerCategorias_deberiaRetornar500CuandoElServicioFallaInesperadamente() {
    Timer timer = buildTimerConProducto(1L, 10L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioProductoMock.obtenerCategoriasDeUnProductoDisponiblesParaImportar(
        10L,
        "group-uuid-test"
      )
    )
      .thenThrow(new RuntimeException("Error de base de datos"));

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
  }

  // =========================================================
  // EDGE CASES
  // =========================================================

  @Test
  @DisplayName(
    "EDGE-01 | obtenerCategoriasDeUnProducto | Funciona correctamente con ID igual a 1 (mínimo válido)"
  )
  void obtenerCategorias_deberiaFuncionarConIdMinimo() {
    Timer timer = buildTimerConProducto(1L, 10L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(servicioProductoMock.obtenerCategoriasDeUnProducto(10L)).thenReturn(buildCategorias());

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
  }

  @Test
  @DisplayName(
    "EDGE-02 | obtenerCategoriasDeUnProducto | Funciona correctamente con ID igual a Long.MAX_VALUE"
  )
  void obtenerCategorias_deberiaFuncionarConIdMaximo() {
    Timer timer = buildTimerConProducto(Long.MAX_VALUE, 10L);

    when(servicioTimerMock.buscarPorId(Long.MAX_VALUE)).thenReturn(timer);
    when(servicioProductoMock.obtenerCategoriasDeUnProducto(10L)).thenReturn(buildCategorias());

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(Long.MAX_VALUE);

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
  }

  @Test
  @DisplayName(
    "EDGE-03 | obtenerCategoriasDeUnProducto | Retorna 404 cuando la lista de categorias esta vacia"
  )
  void obtenerCategorias_deberiaRetornar404CuandoListaDeCategoriasEstaVacia() {
    when(servicioProductoMock.obtenerCategoriasDeUnProducto(1L))
      .thenThrow(new ValidacionException("categorias del producto"));

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
  }

  @Test
  @DisplayName(
    "EDGE-04 | obtenerCategoriasDeUnProducto | El cuerpo de la respuesta contiene la clave 'categorias'"
  )
  void obtenerCategorias_deberiaTenerLaClaveCategoriasEnElCuerpo() {
    Timer timer = buildTimerConProducto(1L, 10L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(servicioProductoMock.obtenerCategoriasDeUnProducto(10L)).thenReturn(buildCategorias());

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    assertNotNull(respuesta.getBody());
    assertTrue(respuesta.getBody().containsKey("categorias"));
    assertFalse(respuesta.getBody().containsKey("categoria")); // verifica que no usa la clave incorrecta
  }

  // =========================================================
  // SECURITY
  // =========================================================

  @Test
  @DisplayName(
    "SEC-01 | obtenerCategoriasDeUnProducto | Retorna 400 con ID negativo extremo (Long.MIN_VALUE)"
  )
  void obtenerCategorias_deberiaRetornar400ConIdNegativoExtremo() {
    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(Long.MIN_VALUE);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName(
    "SEC-02 | obtenerCategoriasDeUnProducto | No llama al servicio cuando el ID no es valido"
  )
  void obtenerCategorias_noDeberiaLlamarAlServicioConIdInvalido() {
    controladorDashboard.obtenerCategoriasDeUnProducto(null);
    controladorDashboard.obtenerCategoriasDeUnProducto(0L);
    controladorDashboard.obtenerCategoriasDeUnProducto(-1L);
    controladorDashboard.obtenerCategoriasDeUnProducto(Long.MIN_VALUE);

    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName(
    "SEC-03 | obtenerCategoriasDeUnProducto | No expone informacion interna en errores 500"
  )
  void obtenerCategorias_noDeberiaExponerInformacionInternaEnErrores500() {
    Timer timer = buildTimerConProducto(1L, 10L);

    when(servicioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioProductoMock.obtenerCategoriasDeUnProductoDisponiblesParaImportar(
        10L,
        "group-uuid-test"
      )
    )
      .thenThrow(new RuntimeException("Error interno con datos sensibles: password=1234"));

    ResponseEntity<Map<String, Object>> respuesta =
      controladorDashboard.obtenerCategoriasDeUnProducto(1L);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
    assertNull(respuesta.getBody());
  }
}
