package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.RepositorioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioControlStock;
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
  private RepositorioReglaVencimiento repositorioReglaVencimientoMock;
  private ServicioControlStock servicioControlStockMock;

  @BeforeEach
  public void init() {
    repositorioTimerMock = mock(RepositorioTimer.class);
    repositorioProductoMock = mock(RepositorioProducto.class);
    repositorioCategoriaMock = mock(RepositorioCategoria.class);
    repositorioReglaVencimientoMock = mock(RepositorioReglaVencimiento.class);
    servicioControlStockMock = mock(ServicioControlStock.class);
    servicioProducto =
      new ServicioProductoImpl(
        repositorioProductoMock,
        repositorioTimerMock,
        repositorioCategoriaMock,
        repositorioReglaVencimientoMock,
        servicioControlStockMock
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
    verify(repositorioReglaVencimientoMock, times(1)).guardar(any(ReglaVencimiento.class));
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
    verify(repositorioReglaVencimientoMock, times(1)).guardar(any(ReglaVencimiento.class));
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

  // --- listarProductos ---

  @Test
  @DisplayName("HAP-08 | listarProductos | Sin filtro delega a obtenerTodos")
  public void listarProductosSinFiltroDeberiaRetornarTodos() {
    List<Producto> todos = Arrays.asList(new Producto(), new Producto(), new Producto());
    when(repositorioProductoMock.obtenerTodos()).thenReturn(todos);

    List<Producto> resultado = servicioProducto.listarProductos(null);

    assertEquals(3, resultado.size());
    verify(repositorioProductoMock, times(1)).obtenerTodos();
    verify(repositorioProductoMock, never()).obtenerProductosPorCategoria(any());
  }

  @Test
  @DisplayName("HAP-09 | listarProductos | Con categoriaId delega a obtenerProductosPorCategoria")
  public void listarProductosConCategoriaDeberiaFiltrarPorCategoria() {
    List<Producto> filtrados = Arrays.asList(new Producto());
    when(repositorioProductoMock.obtenerProductosPorCategoria(1L)).thenReturn(filtrados);

    List<Producto> resultado = servicioProducto.listarProductos(1L);

    assertEquals(1, resultado.size());
    verify(repositorioProductoMock, times(1)).obtenerProductosPorCategoria(1L);
    verify(repositorioProductoMock, never()).obtenerTodos();
  }

  @Test
  @DisplayName("HAP-10 | listarProductos | Retorna lista vacía si no hay productos")
  public void listarProductosSinResultadosDeberiaRetornarListaVacia() {
    when(repositorioProductoMock.obtenerTodos()).thenReturn(Collections.emptyList());

    List<Producto> resultado = servicioProducto.listarProductos(null);

    assertNotNull(resultado);
    assertTrue(resultado.isEmpty());
  }

  // --- agregarStock ---

  @Test
  @DisplayName("HAP-11 | agregarStock | Suma la cantidad al stock existente del producto")
  public void agregarStockDeberiaIncrementarLaCantidad() {
    Producto producto = productoConStock(5L, 10);
    when(repositorioProductoMock.obtenerProductoPorId(5L)).thenReturn(producto);

    servicioProducto.agregarStock(5L, 3);

    assertEquals(13, producto.getCantidad());
    verify(repositorioProductoMock, times(1)).actualizar(producto);
    verify(servicioControlStockMock, times(1))
      .registrarMovimiento(eq(producto), isNull(), eq(3), eq(TipoMovimientoStock.INGRESO));
  }

  @Test
  @DisplayName("HAP-12 | agregarStock | Funciona cuando el stock inicial es cero")
  public void agregarStockConStockCeroDeberiaQuedarEnLaCantidadAgregada() {
    Producto producto = productoConStock(1L, 0);
    when(repositorioProductoMock.obtenerProductoPorId(1L)).thenReturn(producto);

    servicioProducto.agregarStock(1L, 5);

    assertEquals(5, producto.getCantidad());
  }

  @Test
  @DisplayName("NEG-08 | agregarStock | Cantidad cero lanza excepción")
  public void agregarStockConCantidadCeroDeberiaLanzarExcepcion() {
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.agregarStock(1L, 0)
    );
    assertEquals("La cantidad a agregar debe ser mayor a 0", ex.getMessage());
    verify(repositorioProductoMock, never()).actualizar(any());
  }

  @Test
  @DisplayName("NEG-09 | agregarStock | Cantidad negativa lanza excepción")
  public void agregarStockConCantidadNegativaDeberiaLanzarExcepcion() {
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.agregarStock(1L, -5)
    );
    assertEquals("La cantidad a agregar debe ser mayor a 0", ex.getMessage());
    verify(repositorioProductoMock, never()).actualizar(any());
  }

  @Test
  @DisplayName("NEG-10 | agregarStock | Cantidad nula lanza excepción")
  public void agregarStockConCantidadNulaDeberiaLanzarExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> servicioProducto.agregarStock(1L, null));
    verify(repositorioProductoMock, never()).actualizar(any());
  }

  @Test
  @DisplayName("NEG-11 | agregarStock | Producto inexistente lanza excepción")
  public void agregarStockConProductoInexistenteDeberiaLanzarExcepcion() {
    when(repositorioProductoMock.obtenerProductoPorId(99L)).thenReturn(null);

    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.agregarStock(99L, 5)
    );
    assertEquals("Producto no encontrado", ex.getMessage());
  }

  // --- descontarStock ---

  @Test
  @DisplayName("HAP-13 | descontarStock | Resta la cantidad del stock existente")
  public void descontarStockDeberiaReducirLaCantidad() {
    Producto producto = productoConStock(1L, 10);

    servicioProducto.descontarStock(producto, 4);

    assertEquals(6, producto.getCantidad());
  }

  @Test
  @DisplayName("HAP-14 | descontarStock | Permite descontar exactamente el stock disponible")
  public void descontarStockExactoDeberiaDejarEnCero() {
    Producto producto = productoConStock(1L, 5);

    servicioProducto.descontarStock(producto, 5);

    assertEquals(0, producto.getCantidad());
  }

  @Test
  @DisplayName("NEG-12 | descontarStock | Cantidad mayor al stock lanza excepción")
  public void descontarStockMayorAlDisponibleDeberiaLanzarExcepcion() {
    Producto producto = productoConStock(1L, 3);

    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.descontarStock(producto, 5)
    );
    assertTrue(ex.getMessage().contains("Stock insuficiente"));
    assertTrue(ex.getMessage().contains("3"));
    assertTrue(ex.getMessage().contains("5"));
  }

  @Test
  @DisplayName("NEG-13 | descontarStock | Stock en cero no permite descontar")
  public void descontarStockConStockCeroDeberiaLanzarExcepcion() {
    Producto producto = productoConStock(1L, 0);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.descontarStock(producto, 1)
    );
  }

  // --- quitarStock ---

  @Test
  @DisplayName("HAP-17 | quitarStock | Resta la cantidad y registra EGRESO en ControlStock")
  public void quitarStockDeberiaRestarYRegistrarEgreso() {
    Producto producto = productoConStock(1L, 10);
    when(repositorioProductoMock.obtenerProductoPorId(1L)).thenReturn(producto);

    servicioProducto.quitarStock(1L, 4);

    assertEquals(6, producto.getCantidad());
    verify(repositorioProductoMock, times(1)).actualizar(producto);
    verify(servicioControlStockMock, times(1))
      .registrarMovimiento(eq(producto), isNull(), eq(4), eq(TipoMovimientoStock.EGRESO));
  }

  @Test
  @DisplayName("HAP-18 | quitarStock | Permite quitar exactamente el stock disponible")
  public void quitarStockExactoDeberiaDejarEnCero() {
    Producto producto = productoConStock(1L, 5);
    when(repositorioProductoMock.obtenerProductoPorId(1L)).thenReturn(producto);

    servicioProducto.quitarStock(1L, 5);

    assertEquals(0, producto.getCantidad());
  }

  @Test
  @DisplayName("NEG-15 | quitarStock | Cantidad mayor al stock lanza excepción")
  public void quitarStockMayorAlDisponibleDeberiaLanzarExcepcion() {
    Producto producto = productoConStock(1L, 3);
    when(repositorioProductoMock.obtenerProductoPorId(1L)).thenReturn(producto);

    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.quitarStock(1L, 5)
    );
    assertTrue(ex.getMessage().contains("Stock insuficiente"));
    verify(repositorioProductoMock, never()).actualizar(any());
  }

  @Test
  @DisplayName("NEG-16 | quitarStock | Cantidad cero lanza excepción")
  public void quitarStockConCantidadCeroDeberiaLanzarExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> servicioProducto.quitarStock(1L, 0));
    verify(repositorioProductoMock, never()).actualizar(any());
  }

  @Test
  @DisplayName("NEG-17 | quitarStock | Producto inexistente lanza excepción")
  public void quitarStockConProductoInexistenteDeberiaLanzarExcepcion() {
    when(repositorioProductoMock.obtenerProductoPorId(99L)).thenReturn(null);

    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.quitarStock(99L, 1)
    );
    assertEquals("Producto no encontrado", ex.getMessage());
  }

  // --- validarCantidad (via crearProducto) ---

  @Test
  @DisplayName("NEG-14 | validarCantidad | Cantidad negativa al crear producto lanza excepción")
  public void crearProductoConCantidadNegativaDeberiaLanzarExcepcion() {
    ProductoDto datos = datoValidos();
    datos.setCantidad(-1);

    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.crearProducto(datos)
    );
    assertEquals("La cantidad no puede ser negativa", ex.getMessage());
  }

  @Test
  @DisplayName("HAP-15 | validarCantidad | Cantidad cero al crear producto es válida")
  public void crearProductoConCantidadCeroDeberiaGuardar() {
    ProductoDto datos = datoValidos();
    datos.setCantidad(0);
    when(repositorioCategoriaMock.obtenerCategoriasPorIds(datos.getCategoriasIds()))
      .thenReturn(Set.of(new Categoria()));

    servicioProducto.crearProducto(datos);

    verify(repositorioProductoMock, times(1)).guardar(any(Producto.class));
  }

  @Test
  @DisplayName("HAP-16 | validarCantidad | Sin cantidad informada se guarda con 0 por defecto")
  public void crearProductoSinCantidadDeberiaGuardarConCero() {
    ProductoDto datos = datoValidos();
    datos.setCantidad(null);
    when(repositorioCategoriaMock.obtenerCategoriasPorIds(datos.getCategoriasIds()))
      .thenReturn(Set.of(new Categoria()));

    servicioProducto.crearProducto(datos);

    verify(repositorioProductoMock, times(1)).guardar(argThat(p -> p.getCantidad() == 0));
  }

  // --- Helper ---

  private Producto productoConStock(Long id, int cantidad) {
    Producto p = new Producto();
    p.setId(id);
    p.setCantidad(cantidad);
    return p;
  }

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
