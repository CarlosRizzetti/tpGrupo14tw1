package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.services.ServicioTimer;
import com.tallerwebi.presentacion.controller.ControladorProducto;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

public class ControladorProductoTest {

  private ControladorProducto controladorProducto;
  private ServicioProducto servicioProductoMock;
  private ServicioCategoria servicioCategoriaMock;
  private HttpSession sessionMock;
  private Usuario usuarioAdminMock;
  private ServicioTimer servicioTimerMock;

  @BeforeEach
  public void init() {
    servicioProductoMock = mock(ServicioProducto.class);
    servicioCategoriaMock = mock(ServicioCategoria.class);
    servicioTimerMock = mock(ServicioTimer.class);
    sessionMock = mock(HttpSession.class);
    controladorProducto =
      new ControladorProducto(servicioProductoMock, servicioCategoriaMock, servicioTimerMock);
    Categoria categoriaDefault = new Categoria("default.png", true, "default");
    CategoriaDto categoriaDefaultDTO = new CategoriaDto(categoriaDefault);
    List<CategoriaDto> categorias = List.of(categoriaDefaultDTO);
    usuarioAdminMock = mock(Usuario.class);
    when(usuarioAdminMock.getRol()).thenReturn("ADMIN");
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioAdminMock);
    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu()).thenReturn(categorias);
  }

  private Timer buildTimerConProducto(Long timerId, Long productoId) {
    Producto producto = new Producto();
    producto.setId(productoId);

    Timer timer = new Timer();
    timer.setId(timerId);
    timer.setProducto(producto);
    timer.setGroupId("group-uuid-test");
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

  // --- GET /producto/nuevo ---

  @Test
  public void mostrarFormularioComoAdminDeberiaRetornarVistaNuevoProducto() {
    when(sessionMock.getAttribute("ROL")).thenReturn("admin");

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario(sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("producto/nuevo"));
  }

  @Test
  public void mostrarFormularioDeberiaIncluirListaDeCategorias() {
    when(sessionMock.getAttribute("ROL")).thenReturn("admin");

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario(sessionMock);

    // validacion
    assertThat(mav.getModel().get("categorias"), notNullValue());
  }

  @Test
  public void mostrarFormularioDeberiaIncluirDatosProductoVacio() {
    when(sessionMock.getAttribute("ROL")).thenReturn("admin");

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario(sessionMock);

    // validacion
    assertThat(mav.getModel().get("datosProducto"), instanceOf(ProductoDto.class));
  }

  @Test
  public void mostrarFormularioSinSesionDeberiaRedirigirAAccesoDenegado() {
    // preparacion
    when(sessionMock.getAttribute("usuario")).thenReturn(null);

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario(sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/acceso-denegado"));
  }

  @Test
  public void mostrarFormularioComoUsuarioNoAdminDeberiaRedirigirAAccesoDenegado() {
    // preparacion
    Usuario usuarioComun = mock(Usuario.class);
    when(usuarioComun.getRol()).thenReturn("USER");
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioComun);

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario(sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/acceso-denegado"));
  }

  // --- POST /producto/nuevo ---

  @Test
  public void crearProductoExitosoDeberiaRedirigirAExito() {
    // preparacion
    ProductoDto datos = new ProductoDto();
    when(sessionMock.getAttribute("ROL")).thenReturn("admin");

    // ejecucion
    ModelAndView mav = controladorProducto.crearProducto(datos, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/producto/exito"));
    verify(servicioProductoMock, times(1)).crearProducto(datos);
  }

  @Test
  public void crearProductoConErrorDeValidacionDeberiaVolverAlFormulario() {
    when(sessionMock.getAttribute("ROL")).thenReturn("admin");

    // preparacion
    ProductoDto datos = new ProductoDto();
    doThrow(new IllegalArgumentException("El nombre del producto es obligatorio"))
      .when(servicioProductoMock)
      .crearProducto(datos);

    // ejecucion
    ModelAndView mav = controladorProducto.crearProducto(datos, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("producto/nuevo"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalToIgnoringCase("El nombre del producto es obligatorio")
    );
  }

  @Test
  public void crearProductoSinSesionDeberiaRedirigirAAccesoDenegado() {
    // preparacion
    when(sessionMock.getAttribute("usuario")).thenReturn(null);

    // ejecucion
    ModelAndView mav = controladorProducto.crearProducto(new ProductoDto(), sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/acceso-denegado"));
    verify(servicioProductoMock, never()).crearProducto(any());
  }

  // --- GET /category/{id}/products ---

  @Test
  public void mostrarProductosPorCategoriaDeberiaRetornarVistaProductosConLista() {
    // preparacion
    Long categoriaId = 1L;
    List<com.tallerwebi.dominio.entity.Producto> productosMock = Collections.singletonList(
      new com.tallerwebi.dominio.entity.Producto()
    );
    when(servicioProductoMock.obtenerProductosPorCategoria(categoriaId)).thenReturn(productosMock);

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarProductosPorCategoria(categoriaId, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("productos"));
    assertThat(mav.getModel().get("productos"), notNullValue());
    assertThat((List<?>) mav.getModel().get("productos"), hasSize(1));
    verify(servicioProductoMock, times(1)).obtenerProductosPorCategoria(categoriaId);
  }

  // =========================================================
  // HAPPY PATH
  // =========================================================

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(null);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName("NEG-02 | obtenerCategoriasDeUnProducto | Retorna 400 cuando el ID es cero")
  void obtenerCategorias_deberiaRetornar400CuandoIdEsCero() {
    ResponseEntity<Map<String, Object>> respuesta =
      controladorProducto.obtenerCategoriasDeUnProducto(0L);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName("NEG-03 | obtenerCategoriasDeUnProducto | Retorna 400 cuando el ID es negativo")
  void obtenerCategorias_deberiaRetornar400CuandoIdEsNegativo() {
    ResponseEntity<Map<String, Object>> respuesta =
      controladorProducto.obtenerCategoriasDeUnProducto(-1L);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName("NEG-04 | obtenerCategoriasDeUnProducto | Retorna 404 cuando el timer no existe")
  void obtenerCategorias_deberiaRetornar404CuandoTimerNoExiste() {
    when(servicioTimerMock.buscarPorId(99L)).thenReturn(null);

    ResponseEntity<Map<String, Object>> respuesta =
      controladorProducto.obtenerCategoriasDeUnProducto(99L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(Long.MAX_VALUE);

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

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
      controladorProducto.obtenerCategoriasDeUnProducto(Long.MIN_VALUE);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    verifyNoInteractions(servicioProductoMock);
  }

  @Test
  @DisplayName(
    "SEC-02 | obtenerCategoriasDeUnProducto | No llama al servicio cuando el ID no es valido"
  )
  void obtenerCategorias_noDeberiaLlamarAlServicioConIdInvalido() {
    controladorProducto.obtenerCategoriasDeUnProducto(null);
    controladorProducto.obtenerCategoriasDeUnProducto(0L);
    controladorProducto.obtenerCategoriasDeUnProducto(-1L);
    controladorProducto.obtenerCategoriasDeUnProducto(Long.MIN_VALUE);

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
      controladorProducto.obtenerCategoriasDeUnProducto(1L);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
    assertNull(respuesta.getBody());
  }
}
