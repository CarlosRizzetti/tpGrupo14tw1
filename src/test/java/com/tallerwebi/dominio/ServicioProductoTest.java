package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.services.ServicioProductoImpl;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioProductoTest {

  private ServicioProductoImpl servicioProducto;
  private RepositorioProducto repositorioProductoMock;
  private RepositorioTimer repositorioTimerMock;
  private Clock clock;
  private RepositorioCategoria repositorioCategoriaMock;

  @BeforeEach
  public void init() {
    clock = Clock.fixed(Instant.now(), ZoneOffset.ofHours(-3));
    repositorioTimerMock = mock(RepositorioTimer.class);
    repositorioProductoMock = mock(RepositorioProducto.class);
    repositorioCategoriaMock = mock(RepositorioCategoria.class);
    servicioProducto =
      new ServicioProductoImpl(
        clock,
        repositorioProductoMock,
        repositorioTimerMock,
        repositorioCategoriaMock
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

  // --- Validaciones de Regla de Vencimiento ---

  @Test
  public void crearProductoSinUbicacionDeberiaLanzarExcepcion() {
    // preparacion
    ProductoDto datos = datoValidos();
    datos.setUbicacion(null);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.crearProducto(datos)
    );
    assertThat(ex.getMessage(), equalToIgnoringCase("La ubicación es obligatoria"));
  }

  @Test
  public void crearProductoConDuracionCeroDeberiaLanzarExcepcion() {
    // preparacion
    ProductoDto datos = datoValidos();
    datos.setDuracionMinutos(0);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.crearProducto(datos)
    );
    assertThat(ex.getMessage(), equalToIgnoringCase("La duración debe ser mayor a 0"));
  }

  // --- Validaciones de Descongelamiento ---

  @Test
  public void crearProductoConDescongelamientoActivoSinMinutosDeberiaLanzarExcepcion() {
    // preparacion
    ProductoDto datos = datoValidos();
    datos.setTieneDescongelamiento(true);
    datos.setDescongelamientoMinutos(null);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.crearProducto(datos)
    );
    assertThat(
      ex.getMessage(),
      equalToIgnoringCase("Los minutos de descongelamiento son obligatorios")
    );
  }

  @Test
  public void crearProductoConDescongelamientoActivoConMinutosCeroDeberiaLanzarExcepcion() {
    // preparacion
    ProductoDto datos = datoValidos();
    datos.setTieneDescongelamiento(true);
    datos.setDescongelamientoMinutos(0);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioProducto.crearProducto(datos)
    );
    assertThat(
      ex.getMessage(),
      equalToIgnoringCase("Los minutos de descongelamiento son obligatorios")
    );
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

  @Test
  public void queSeGenereUnTimerCorrectamenteSegunLaReglaDeVencimiento() {
    ReglaVencimiento regla1 = new ReglaVencimiento()
      .builder()
      .id(1L)
      .ubicacion("Mesa")
      .duracionMinutos(120)
      .tieneDescongelamiento(false)
      .descongelamientoMinutos(0)
      .build();
    ReglaVencimiento regla2 = new ReglaVencimiento()
      .builder()
      .id(2L)
      .ubicacion("Heladera")
      .duracionMinutos(1080)
      .tieneDescongelamiento(true)
      .descongelamientoMinutos(120)
      .build();
    Categoria categoria = new Categoria()
      .builder()
      .id(1L)
      .icono("ejemplo")
      .estaActiva(true)
      .nombre("Cocina")
      .build();
    Producto producto = new Producto()
      .builder()
      .id(1L)
      .nombre("Dona")
      .estaActivo(true)
      .reglas(Set.of(regla1, regla2))
      .categorias(Set.of(categoria))
      .build();
    categoria.setProductos(List.of(producto));
    OffsetDateTime fechaCreacion = OffsetDateTime.now(clock);
    OffsetDateTime fechaVencimiento = fechaCreacion.plusMinutes(1080);
    OffsetDateTime descongelamiento = fechaCreacion.plusMinutes(120);
    Timer timer = new Timer(
      fechaCreacion,
      fechaVencimiento,
      descongelamiento,
      producto,
      categoria,
      regla2
    );

    Timer timerGenerado = this.servicioProducto.generarVencimiento(producto, categoria, 2L, 0);

    assertEquals(timer.getFechaVencimiento(), timerGenerado.getFechaVencimiento());
    assertEquals(timer.getFechaCreacion(), timerGenerado.getFechaCreacion());
    assertEquals(timer.getReglaVencimiento(), timerGenerado.getReglaVencimiento());
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
