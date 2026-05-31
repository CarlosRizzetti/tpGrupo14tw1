package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.services.ServicioProductoImpl;
import com.tallerwebi.presentacion.dto.ProductoDto;
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
