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
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
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
  private ServicioReglaVencimiento servicioReglaVencimientoMock;

  @BeforeEach
  public void init() {
    servicioProductoMock = mock(ServicioProducto.class);
    servicioCategoriaMock = mock(ServicioCategoria.class);
    servicioTimerMock = mock(ServicioTimer.class);
    servicioReglaVencimientoMock = mock(ServicioReglaVencimiento.class);
    sessionMock = mock(HttpSession.class);
    controladorProducto =
      new ControladorProducto(
        servicioProductoMock,
        servicioCategoriaMock,
        servicioTimerMock,
        servicioReglaVencimientoMock
      );
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
}
