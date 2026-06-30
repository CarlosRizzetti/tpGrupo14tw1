package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.interfaces.*;
import com.tallerwebi.presentacion.controller.ControladorProducto;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
  private ServicioReglaVencimiento servicioReglaVencimientoMock;
  private ServicioUsuario servicioUsuarioMock;

  @BeforeEach
  public void init() {
    servicioProductoMock = mock(ServicioProducto.class);
    servicioCategoriaMock = mock(ServicioCategoria.class);
    servicioReglaVencimientoMock = mock(ServicioReglaVencimiento.class);
    sessionMock = mock(HttpSession.class);
    servicioUsuarioMock = mock(ServicioUsuario.class);
    controladorProducto =
      new ControladorProducto(
        servicioProductoMock,
        servicioCategoriaMock,
        servicioReglaVencimientoMock,
        servicioUsuarioMock
      );
    Categoria categoriaDefault = new Categoria("default.png", true, "default");
    CategoriaDto categoriaDefaultDTO = new CategoriaDto(categoriaDefault);
    List<CategoriaDto> categorias = List.of(categoriaDefaultDTO);
    usuarioAdminMock = mock(Usuario.class);
    when(usuarioAdminMock.getRol()).thenReturn("ADMIN");
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioAdminMock);
    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu()).thenReturn(categorias);
    when(servicioUsuarioMock.obtenerUsuarioPorEmail(any())).thenReturn(new Usuario());
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
    ModelAndView mav = controladorProducto.mostrarFormulario();

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("funcionalidadesAdmin/producto/nuevo"));
  }

  @Test
  public void mostrarFormularioDeberiaIncluirListaDeCategorias() {
    when(sessionMock.getAttribute("ROL")).thenReturn("admin");

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario();

    // validacion
    assertThat(mav.getModel().get("categorias"), notNullValue());
  }

  @Test
  public void mostrarFormularioDeberiaIncluirDatosProductoVacio() {
    when(sessionMock.getAttribute("ROL")).thenReturn("admin");

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarFormulario();

    // validacion
    assertThat(mav.getModel().get("datosProducto"), instanceOf(ProductoDto.class));
  }

  // --- POST /producto/nuevo ---

  @Test
  public void crearProductoExitosoDeberiaRedirigirAExito() {
    // preparacion
    ProductoDto datos = new ProductoDto();
    when(sessionMock.getAttribute("ROL")).thenReturn("admin");

    // ejecucion
    ModelAndView mav = controladorProducto.crearProducto(datos);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/producto/exito"));
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
    ModelAndView mav = controladorProducto.crearProducto(datos);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("funcionalidadesAdmin/producto/nuevo"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalToIgnoringCase("El nombre del producto es obligatorio")
    );
  }

  // --- GET /category/{id}/products ---

  @Test
  public void mostrarProductosPorCategoriaSiNoEstaAutenticadoDeberiaRedirigirALogin() {
    // preparacion
    org.springframework.security.core.Authentication auth = null;

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarProductosPorCategoria(1L, sessionMock, auth);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void mostrarProductosPorCategoriaSiNoEstaAutenticadoPeroAuthExisteDeberiaRedirigirALogin() {
    // preparacion
    org.springframework.security.core.Authentication auth = mock(
      org.springframework.security.core.Authentication.class
    );
    when(auth.isAuthenticated()).thenReturn(false);

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarProductosPorCategoria(1L, sessionMock, auth);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void mostrarProductosPorCategoriaSiEsAdminDeberiaRetornarVista() {
    // preparacion
    org.springframework.security.core.Authentication auth = mock(
      org.springframework.security.core.Authentication.class
    );
    when(auth.isAuthenticated()).thenReturn(true);
    org.springframework.security.core.GrantedAuthority authority = mock(
      org.springframework.security.core.GrantedAuthority.class
    );
    when(authority.getAuthority()).thenReturn("ROLE_ADMIN");
    doReturn(Collections.singleton(authority)).when(auth).getAuthorities();

    Long categoriaId = 1L;
    when(servicioCategoriaMock.obtenerCategoriaPorId(categoriaId)).thenReturn(new CategoriaDto());
    when(servicioProductoMock.obtenerProductosPorCategoria(categoriaId))
      .thenReturn(Collections.emptyList());
    when(servicioCategoriaMock.obtenerUsuariosPorCategoria(categoriaId))
      .thenReturn(Collections.emptyList());

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarProductosPorCategoria(
      categoriaId,
      sessionMock,
      auth
    );

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("listadoDeProductosYReglas/productos"));
  }

  @Test
  public void mostrarProductosPorCategoriaSiUsuarioSinPermisoDeberiaRedirigirAHome() {
    // preparacion
    org.springframework.security.core.Authentication auth = mock(
      org.springframework.security.core.Authentication.class
    );
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getName()).thenReturn("user@test.com");
    org.springframework.security.core.GrantedAuthority authority = mock(
      org.springframework.security.core.GrantedAuthority.class
    );
    when(authority.getAuthority()).thenReturn("ROLE_USER");
    doReturn(Collections.singleton(authority)).when(auth).getAuthorities();

    // El usuario tiene categorias, pero ninguna coincide con la ID solicitada
    CategoriaDto catUsuario = new CategoriaDto();
    catUsuario.setId(2L);
    when(servicioCategoriaMock.obtenerCategoriasPorUsuario("user@test.com"))
      .thenReturn(Collections.singletonList(catUsuario));

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarProductosPorCategoria(1L, sessionMock, auth);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home"));
  }

  @Test
  public void mostrarProductosPorCategoriaSiUsuarioConPermisoDeberiaRetornarVista() {
    // preparacion
    org.springframework.security.core.Authentication auth = mock(
      org.springframework.security.core.Authentication.class
    );
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getName()).thenReturn("user@test.com");
    org.springframework.security.core.GrantedAuthority authority = mock(
      org.springframework.security.core.GrantedAuthority.class
    );
    when(authority.getAuthority()).thenReturn("ROLE_USER");
    doReturn(Collections.singleton(authority)).when(auth).getAuthorities();

    // El usuario tiene la categoria solicitada
    Long categoriaId = 1L;
    CategoriaDto catUsuario = new CategoriaDto();
    catUsuario.setId(categoriaId);
    when(servicioCategoriaMock.obtenerCategoriasPorUsuario("user@test.com"))
      .thenReturn(Collections.singletonList(catUsuario));
    when(servicioCategoriaMock.obtenerCategoriaPorId(categoriaId)).thenReturn(catUsuario);
    when(servicioProductoMock.obtenerProductosPorCategoria(categoriaId))
      .thenReturn(Collections.emptyList());
    when(servicioCategoriaMock.obtenerUsuariosPorCategoria(categoriaId))
      .thenReturn(Collections.emptyList());

    // ejecucion
    ModelAndView mav = controladorProducto.mostrarProductosPorCategoria(
      categoriaId,
      sessionMock,
      auth
    );

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("listadoDeProductosYReglas/productos"));
  }
}
