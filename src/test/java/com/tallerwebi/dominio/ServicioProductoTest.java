package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.services.ServicioProductoImpl;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioProductoTest {

  private ServicioProductoImpl servicioProducto;
  private RepositorioProducto repositorioProductoMock;

  @BeforeEach
  public void init() {
    repositorioProductoMock = mock(RepositorioProducto.class);
    servicioProducto = new ServicioProductoImpl(repositorioProductoMock);
  }

  // --- Casos felices ---

  @Test
  public void crearProductoConDatosValidosDeberiaGuardarEnLaBD() {
    // preparacion
    ProductoDto datos = datoValidos();
    when(repositorioProductoMock.obtenerCategoriasPorIds(datos.getCategoriasIds()))
      .thenReturn(Arrays.asList(new Categoria()));

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
    when(repositorioProductoMock.obtenerCategoriasPorIds(datos.getCategoriasIds()))
      .thenReturn(Arrays.asList(new Categoria()));

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
