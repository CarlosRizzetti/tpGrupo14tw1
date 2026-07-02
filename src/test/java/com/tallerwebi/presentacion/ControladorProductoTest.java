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

  // --- /admin/producto/exito ---
  @Test
  public void exitoDeberiaRetornarVistaExito() {
    ModelAndView mav = controladorProducto.exito();
    assertThat(mav.getViewName(), equalToIgnoringCase("funcionalidadesAdmin/producto/exito"));
  }

  // --- /admin/productos ---
  @Test
  public void gestionProductosDeberiaRetornarVistaYModeloCorrectos() {
    Long categoriaId = 1L;
    when(servicioProductoMock.listarProductos(categoriaId)).thenReturn(Collections.emptyList());
    when(servicioCategoriaMock.obtenerLasCategoriasParaElMenu())
      .thenReturn(Collections.emptyList());

    ModelAndView mav = controladorProducto.gestionProductos(categoriaId);

    assertThat(mav.getViewName(), equalToIgnoringCase("funcionalidadesAdmin/producto/gestion"));
    assertThat(mav.getModel().get("productos"), notNullValue());
    assertThat(mav.getModel().get("categorias"), notNullValue());
    assertThat(mav.getModel().get("categoriaSeleccionada"), equalTo(categoriaId));
  }

  // --- agregarStock ---
  @Test
  public void agregarStockDeberiaRedirigirConCategoria() {
    String redirect = controladorProducto.agregarStock(1L, 10, 2L);
    verify(servicioProductoMock).agregarStock(1L, 10);
    assertThat(redirect, equalToIgnoringCase("redirect:/admin/productos?categoriaId=2"));
  }

  @Test
  public void agregarStockDeberiaRedirigirSinCategoria() {
    String redirect = controladorProducto.agregarStock(1L, 10, null);
    verify(servicioProductoMock).agregarStock(1L, 10);
    assertThat(redirect, equalToIgnoringCase("redirect:/admin/productos"));
  }

  // --- quitarStock ---
  @Test
  public void quitarStockDeberiaRedirigirConCategoria() {
    String redirect = controladorProducto.quitarStock(1L, 10, 2L);
    verify(servicioProductoMock).quitarStock(1L, 10);
    assertThat(redirect, equalToIgnoringCase("redirect:/admin/productos?categoriaId=2"));
  }

  @Test
  public void quitarStockDeberiaRedirigirSinCategoria() {
    String redirect = controladorProducto.quitarStock(1L, 10, null);
    verify(servicioProductoMock).quitarStock(1L, 10);
    assertThat(redirect, equalToIgnoringCase("redirect:/admin/productos"));
  }

  // --- mostrarVencimientoProducto ---
  @Test
  public void mostrarVencimientoProductoDeberiaRetornarVista() {
    Long productoId = 1L;
    Producto producto = new Producto();
    producto.setReglas(Collections.emptySet());
    when(servicioProductoMock.obtenerProductoConReglas(productoId)).thenReturn(producto);

    CategoriaDto catDto = new CategoriaDto();
    when(sessionMock.getAttribute("categoria")).thenReturn(catDto);

    ModelAndView mav = controladorProducto.mostrarVencimientoProducto(productoId, sessionMock);

    assertThat(
      mav.getViewName(),
      equalToIgnoringCase("listadoDeProductosYReglas/producto-vencimiento")
    );
    assertThat(mav.getModel().get("producto"), notNullValue());
    assertThat(mav.getModel().get("reglas"), notNullValue());
    assertThat(mav.getModel().get("categoria"), equalTo(catDto));
  }

  // --- imprimirConstancia ---
  @Test
  public void imprimirConstanciaDeberiaGenerarVencimientoYRedirigir() {
    org.springframework.security.core.Authentication auth = mock(
      org.springframework.security.core.Authentication.class
    );
    org.springframework.security.core.userdetails.User principalMock = mock(
      org.springframework.security.core.userdetails.User.class
    );
    when(principalMock.getUsername()).thenReturn("test@test.com");
    when(auth.getPrincipal()).thenReturn(principalMock);
    when(auth.getName()).thenReturn("test@test.com");

    Producto producto = new Producto();
    Categoria categoria = new Categoria();
    categoria.setId(2L);
    producto.setCategorias(Collections.singleton(categoria));

    when(servicioProductoMock.obtenerProductoConReglas(1L)).thenReturn(producto);
    Usuario usuario = new Usuario();
    when(servicioUsuarioMock.obtenerUsuarioPorEmail(anyString())).thenReturn(usuario);

    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrsMock = mock(
      org.springframework.web.servlet.mvc.support.RedirectAttributes.class
    );
    String redirect = controladorProducto.imprimirConstancia(
      1L,
      30,
      2L,
      3L,
      5,
      auth,
      redirectAttrsMock
    );

    verify(servicioReglaVencimientoMock)
      .generarVencimiento(producto, categoria, 3L, 30, 5, usuario);
    assertThat(redirect, equalToIgnoringCase("redirect:/dashboard"));
  }
}
