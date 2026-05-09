package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import java.util.Collections;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorProductoTest {

  private ControladorProducto controladorProducto;
  private ServicioProducto servicioProductoMock;
  private ServicioCategoria servicioCategoriaMock;
  private HttpSession sessionMock;
  private Usuario usuarioAdminMock;

  @BeforeEach
  public void init() {
    servicioProductoMock = mock(ServicioProducto.class);
    servicioCategoriaMock = mock(ServicioCategoria.class);
    sessionMock = mock(HttpSession.class);
    controladorProducto = new ControladorProducto(servicioProductoMock, servicioCategoriaMock);

    usuarioAdminMock = mock(Usuario.class);
    when(usuarioAdminMock.getRol()).thenReturn("ADMIN");
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioAdminMock);
    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu())
      .thenReturn(Collections.emptyList());
  }

  // --- GET /producto/nuevo ---

  @Test
  public void mostrarFormularioComoAdminDeberiaRetornarVistaNuevoProducto() {
    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario(sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("producto/nuevo"));
  }

  @Test
  public void mostrarFormularioDeberiaIncluirListaDeCategorias() {
    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario(sessionMock);

    // validacion
    assertThat(mav.getModel().get("categorias"), notNullValue());
  }

  @Test
  public void mostrarFormularioDeberiaIncluirDatosProductoVacio() {
    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario(sessionMock);

    // validacion
    assertThat(mav.getModel().get("datosProducto"), instanceOf(DatosProducto.class));
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
    DatosProducto datos = new DatosProducto();

    // ejecucion
    ModelAndView mav = controladorProducto.crearProducto(datos, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/producto/exito"));
    verify(servicioProductoMock, times(1)).crearProducto(datos);
  }

  @Test
  public void crearProductoConErrorDeValidacionDeberiaVolverAlFormulario() {
    // preparacion
    DatosProducto datos = new DatosProducto();
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
    ModelAndView mav = controladorProducto.crearProducto(new DatosProducto(), sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/acceso-denegado"));
    verify(servicioProductoMock, never()).crearProducto(any());
  }
}
