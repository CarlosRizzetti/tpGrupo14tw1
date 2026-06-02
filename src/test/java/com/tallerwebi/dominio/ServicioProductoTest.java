package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.services.ServicioProductoImpl;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServicioProductoTest {

  private ServicioProductoImpl servicioProducto;
  private RepositorioProducto repositorioProductoMock;
  private RepositorioTimer repositorioTimerMock;
  private RepositorioCategoria repositorioCategoriaMock;
  private ServicioReglaVencimiento servicioReglaVencimientoMock;

  @BeforeEach
  public void init() {
    repositorioTimerMock = mock(RepositorioTimer.class);
    repositorioProductoMock = mock(RepositorioProducto.class);
    repositorioCategoriaMock = mock(RepositorioCategoria.class);
    servicioReglaVencimientoMock = mock(ServicioReglaVencimiento.class);
    servicioProducto =
      new ServicioProductoImpl(
        repositorioProductoMock,
        repositorioTimerMock,
        repositorioCategoriaMock,
        servicioReglaVencimientoMock
      );
  }

  // --- Casos felices ---

  @Test
  public void crearProductoConDatosValidosDeberiaGuardarEnLaBD() {
    // preparacion
    ProductoDto datos = datoValidos();
    when(repositorioCategoriaMock.obtenerCategoriasPorIds(datos.getCategoriasIds()))
      .thenReturn(Set.of(new Categoria()));

    // ejecucion
    servicioProducto.crearProducto(datos);

    // validacion
    verify(repositorioProductoMock, times(1)).guardar(any(Producto.class));
    verify(servicioReglaVencimientoMock, times(1))
      .guardarReglaVencimiento(any(ReglaVencimiento.class));
  }

  @Test
  public void crearProductoSinDescongelamientoDeberiaGuardarCorrectamente() {
    // preparacion
    ProductoDto datos = datoValidos();
    datos.setTieneDescongelamiento(false);
    datos.setDescongelamientoMinutos(null);
    when(repositorioCategoriaMock.obtenerCategoriasPorIds(datos.getCategoriasIds()))
      .thenReturn(Set.of(new Categoria()));

    // ejecucion
    servicioProducto.crearProducto(datos);

    // validacion
    verify(repositorioProductoMock, times(1)).guardar(any(Producto.class));
    verify(servicioReglaVencimientoMock, times(1))
      .guardarReglaVencimiento(any(ReglaVencimiento.class));
  }

  // --- Validaciones de Producto ---

  @Test
  public void crearProductoSinNombreDeberiaLanzarExcepcion() {
    // preparacion
    ProductoDto datos = datoValidos();
    datos.setNombre(null);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.crearProducto(datos)
    );
    assertThat(ex.getMessage(), equalToIgnoringCase("El nombre del producto es obligatorio"));
  }

  @Test
  public void crearProductoConNombreVacioDeberiaLanzarExcepcion() {
    // preparacion
    ProductoDto datos = datoValidos();
    datos.setNombre("   ");

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.crearProducto(datos)
    );
    assertThat(ex.getMessage(), equalToIgnoringCase("El nombre del producto es obligatorio"));
  }

  @Test
  public void crearProductoSinCategoriasDeberiaLanzarExcepcion() {
    // preparacion
    ProductoDto datos = datoValidos();
    datos.setCategoriasIds(Collections.emptyList());

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.crearProducto(datos)
    );
    assertThat(ex.getMessage(), equalToIgnoringCase("Debe seleccionar al menos una categoría"));
  }

  // --- Tests de Obtener Productos por Categoría ---

  @Test
  public void obtenerProductosPorCategoriaDeberiaDelegarAlRepositorioYRetornarLista() {
    // preparacion
    Long categoriaId = 1L;
    List<Producto> productosEsperados = Arrays.asList(new Producto(), new Producto());
    when(repositorioProductoMock.obtenerProductosPorCategoria(categoriaId))
      .thenReturn(productosEsperados);

    // ejecucion
    List<Producto> resultado = servicioProducto.obtenerProductosPorCategoria(categoriaId);

    // validacion
    verify(repositorioProductoMock, times(1)).obtenerProductosPorCategoria(categoriaId);
    assertThat(resultado.size(), org.hamcrest.Matchers.equalTo(2));
  }

  // Agregar estos tests a la clase ServicioProductoTest existente

  @Test
  @DisplayName(
    "HAP-01 | obtenerCategoriasDeUnProducto | Producto con categorías devuelve lista de CategoriaDtos"
  )
  public void obtenerCategoriasDeUnProductoConCategoriasDeberiaRetornarLista() {
    Categoria categoria1 = new Categoria("cocina.png", true, "Cocina");
    categoria1.setId(1L);
    Categoria categoria2 = new Categoria("isla.png", true, "Isla");
    categoria2.setId(2L);
    Producto producto = new Producto();
    producto.setCategorias(Set.of(categoria1, categoria2));

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);

    List<CategoriaDto> resultado = servicioProducto.obtenerCategoriasDeUnProducto(1L);

    assertNotNull(resultado);
    assertEquals(2, resultado.size());
    verify(repositorioProductoMock, times(1)).obtenerProductoConReglasYCategorias(1L);
  }

  @Test
  @DisplayName(
    "HAP-02 | obtenerCategoriasDeUnProducto | Producto con una sola categoría devuelve lista de un elemento"
  )
  public void obtenerCategoriasDeUnProductoConUnaCategoriaDeberiaRetornarListaDeUno() {
    Categoria categoria = new Categoria("cocina.png", true, "Cocina");
    categoria.setId(1L);
    Producto producto = new Producto();
    producto.setCategorias(Set.of(categoria));

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);

    List<CategoriaDto> resultado = servicioProducto.obtenerCategoriasDeUnProducto(1L);

    assertEquals(1, resultado.size());
    assertEquals("Cocina", resultado.get(0).getNombre());
  }

  @Test
  @DisplayName(
    "NEG-01 | obtenerCategoriasDeUnProducto | Producto inexistente lanza ValidacionException"
  )
  public void obtenerCategoriasDeProductoInexistenteDeberiaLanzarExcepcion() {
    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(99L)).thenReturn(null);

    assertThrows(
      ValidacionException.class,
      () -> servicioProducto.obtenerCategoriasDeUnProducto(99L)
    );
  }

  @Test
  @DisplayName(
    "NEG-02 | obtenerCategoriasDeUnProducto | Producto con categorías nulas lanza ValidacionException"
  )
  public void obtenerCategoriasDeProductoConCategoriasNulasDeberiaLanzarExcepcion() {
    Producto producto = new Producto();
    producto.setCategorias(null);

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);

    assertThrows(
      ValidacionException.class,
      () -> servicioProducto.obtenerCategoriasDeUnProducto(1L)
    );
  }

  @Test
  @DisplayName(
    "NEG-03 | obtenerCategoriasDeUnProducto | Producto con set de categorías vacío lanza ValidacionException"
  )
  public void obtenerCategoriasDeProductoConSetVacioDeberiaLanzarExcepcion() {
    Producto producto = new Producto();
    producto.setCategorias(Collections.emptySet());

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);

    assertThrows(
      ValidacionException.class,
      () -> servicioProducto.obtenerCategoriasDeUnProducto(1L)
    );
  }

  @Test
  @DisplayName(
    "SEC-01 | obtenerCategoriasDeUnProducto | Id nulo lanza IdInvalido sin acceder al repositorio"
  )
  public void obtenerCategoriasConIdNuloNoDeberiaAccederAlRepositorio() {
    assertThrows(Exception.class, () -> servicioProducto.obtenerCategoriasDeUnProducto(null));
    verify(repositorioProductoMock, never()).obtenerProductoConReglasYCategorias(any());
  }

  @Test
  @DisplayName(
    "SEC-02 | obtenerCategoriasDeUnProducto | Id negativo lanza IdInvalido sin acceder al repositorio"
  )
  public void obtenerCategoriasConIdNegativoNoDeberiaAccederAlRepositorio() {
    assertThrows(Exception.class, () -> servicioProducto.obtenerCategoriasDeUnProducto(-1L));
    verify(repositorioProductoMock, never()).obtenerProductoConReglasYCategorias(any());
  }

  // --- obtenerCategoriasDeUnProductoDisponiblesParaImportar ---

  @Test
  @DisplayName(
    "HAP-03 | obtenerCategoriasDeUnProductoDisponiblesParaImportar | Devuelve lista con estaPresente correcto"
  )
  public void obtenerCategoriasDisponiblesDeberiaRetornarListaConEstaPresenteCorrecto() {
    Categoria categoria1 = new Categoria("cocina.png", true, "Cocina");
    categoria1.setId(1L);
    Categoria categoria2 = new Categoria("isla.png", true, "Isla");
    categoria2.setId(2L);
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setCategorias(Set.of(categoria1, categoria2));

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);
    when(repositorioTimerMock.existeTimerActivoEnCategoriaYGrupo(1L, "GROUP-01")).thenReturn(true);
    when(repositorioTimerMock.existeTimerActivoEnCategoriaYGrupo(2L, "GROUP-01")).thenReturn(false);

    List<CategoriaDto> resultado =
      servicioProducto.obtenerCategoriasDeUnProductoDisponiblesParaImportar(1L, "GROUP-01");

    assertNotNull(resultado);
    assertEquals(2, resultado.size());
  }

  @Test
  @DisplayName(
    "HAP-04 | obtenerCategoriasDeUnProductoDisponiblesParaImportar | Categoría con timer activo tiene estaPresente en true"
  )
  public void obtenerCategoriasDisponiblesConTimerActivoDeberiaMarcarEstaPresente() {
    Categoria categoria = new Categoria("cocina.png", true, "Cocina");
    categoria.setId(1L);
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setCategorias(Set.of(categoria));

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);
    when(repositorioTimerMock.existeTimerActivoEnCategoriaYGrupo(1L, "GROUP-01")).thenReturn(true);

    List<CategoriaDto> resultado =
      servicioProducto.obtenerCategoriasDeUnProductoDisponiblesParaImportar(1L, "GROUP-01");

    assertEquals(true, resultado.get(0).getEstaPresente());
  }

  @Test
  @DisplayName(
    "HAP-05 | obtenerCategoriasDeUnProductoDisponiblesParaImportar | Categoría sin timer activo tiene estaPresente en false"
  )
  public void obtenerCategoriasDisponiblesSinTimerActivoDeberiaMarcarEstaPresenteFalse() {
    Categoria categoria = new Categoria("cocina.png", true, "Cocina");
    categoria.setId(1L);
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setCategorias(Set.of(categoria));

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);
    when(repositorioTimerMock.existeTimerActivoEnCategoriaYGrupo(1L, "GROUP-01")).thenReturn(false);

    List<CategoriaDto> resultado =
      servicioProducto.obtenerCategoriasDeUnProductoDisponiblesParaImportar(1L, "GROUP-01");

    assertEquals(false, resultado.get(0).getEstaPresente());
  }

  @Test
  @DisplayName(
    "NEG-04 | obtenerCategoriasDeUnProductoDisponiblesParaImportar | Producto inexistente lanza ValidacionException"
  )
  public void obtenerCategoriasDisponiblesDeProductoInexistenteDeberiaLanzarExcepcion() {
    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(99L)).thenReturn(null);

    assertThrows(
      ValidacionException.class,
      () -> servicioProducto.obtenerCategoriasDeUnProductoDisponiblesParaImportar(99L, "GROUP-01")
    );
  }

  @Test
  @DisplayName(
    "NEG-05 | obtenerCategoriasDeUnProductoDisponiblesParaImportar | Producto con categorías vacías lanza ValidacionException"
  )
  public void obtenerCategoriasDisponiblesConSetVacioDeberiaLanzarExcepcion() {
    Producto producto = new Producto();
    producto.setCategorias(Collections.emptySet());

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);

    assertThrows(
      ValidacionException.class,
      () -> servicioProducto.obtenerCategoriasDeUnProductoDisponiblesParaImportar(1L, "GROUP-01")
    );
  }

  @Test
  @DisplayName(
    "EDGE-01 | obtenerCategoriasDeUnProductoDisponiblesParaImportar | GroupId con caracteres peligrosos lanza ValidacionException"
  )
  public void obtenerCategoriasDisponiblesConGroupIdPeligrosoDeberiaLanzarExcepcion() {
    assertThrows(
      ValidacionException.class,
      () ->
        servicioProducto.obtenerCategoriasDeUnProductoDisponiblesParaImportar(
          1L,
          "<script>alert('xss')</script>"
        )
    );
    verify(repositorioProductoMock, never()).obtenerProductoConReglasYCategorias(any());
  }

  @Test
  @DisplayName(
    "EDGE-02 | obtenerCategoriasDeUnProductoDisponiblesParaImportar | GroupId que excede longitud máxima lanza ValidacionException"
  )
  public void obtenerCategoriasDisponiblesConGroupIdDemasiadoLargoDeberiaLanzarExcepcion() {
    String groupIdLargo = "A".repeat(501);

    assertThrows(
      ValidacionException.class,
      () -> servicioProducto.obtenerCategoriasDeUnProductoDisponiblesParaImportar(1L, groupIdLargo)
    );
    verify(repositorioProductoMock, never()).obtenerProductoConReglasYCategorias(any());
  }

  // --- obtenerProductoPorId ---

  @Test
  @DisplayName("HAP-06 | obtenerProductoPorId | Devuelve el producto del repositorio")
  public void obtenerProductoPorIdDeberiaRetornarProducto() {
    Producto producto = new Producto();
    producto.setId(1L);

    when(repositorioProductoMock.obtenerProductoPorId(1L)).thenReturn(producto);

    Producto resultado = servicioProducto.obtenerProductoPorId(1L);

    assertNotNull(resultado);
    assertEquals(1L, resultado.getId());
    verify(repositorioProductoMock, times(1)).obtenerProductoPorId(1L);
  }

  @Test
  @DisplayName("NEG-06 | obtenerProductoPorId | Producto inexistente devuelve null")
  public void obtenerProductoPorIdInexistenteDeberiaRetornarNull() {
    when(repositorioProductoMock.obtenerProductoPorId(99L)).thenReturn(null);

    Producto resultado = servicioProducto.obtenerProductoPorId(99L);

    assertNull(resultado);
  }

  // --- obtenerProductoConReglas ---

  @Test
  @DisplayName("HAP-07 | obtenerProductoConReglas | Devuelve producto con reglas cuando existe")
  public void obtenerProductoConReglasDeberiaRetornarProducto() {
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setReglas(Set.of(new ReglaVencimiento()));

    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(1L)).thenReturn(producto);

    Producto resultado = servicioProducto.obtenerProductoConReglas(1L);

    assertNotNull(resultado);
    assertEquals(1L, resultado.getId());
    verify(repositorioProductoMock, times(1)).obtenerProductoConReglasYCategorias(1L);
  }

  @Test
  @DisplayName("NEG-07 | obtenerProductoConReglas | Producto inexistente devuelve null")
  public void obtenerProductoConReglasInexistenteDeberiaRetornarNull() {
    when(repositorioProductoMock.obtenerProductoConReglasYCategorias(99L)).thenReturn(null);

    Producto resultado = servicioProducto.obtenerProductoConReglas(99L);

    assertNull(resultado);
  }

  // --- Helper ---

  private ProductoDto datoValidos() {
    ProductoDto datos = new ProductoDto();
    datos.setNombre("Milanesa");
    datos.setCategoriasIds(Arrays.asList(1L, 2L));
    datos.setUbicacion("Freezer A");
    datos.setDuracionMinutos(60);
    datos.setTieneDescongelamiento(true);
    datos.setDescongelamientoMinutos(30);
    return datos;
  }
}
