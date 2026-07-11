package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.interfaces.RepositorioArticulo;
import com.tallerwebi.dominio.services.ServicioArticuloImpl;
import com.tallerwebi.presentacion.dto.StockArticuloDto;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioArticuloImplTest {

  private ServicioArticuloImpl servicioArticulo;
  private RepositorioArticulo repositorioArticuloMock;

  @BeforeEach
  public void init() {
    repositorioArticuloMock = mock(RepositorioArticulo.class);
    servicioArticulo = new ServicioArticuloImpl(repositorioArticuloMock);
  }

  @Test
  public void obtenerTodosLosArticulosDeberiaRetornarListaDeArticulos() {
    List<Articulos> esperados = Collections.singletonList(new Articulos());
    when(repositorioArticuloMock.obtenerTodos()).thenReturn(esperados);

    List<Articulos> obtenidos = servicioArticulo.obtenerTodosLosArticulos();

    assertThat(obtenidos, equalTo(esperados));
  }

  @Test
  public void registrarArticuloDeberiaLlamarAlRepositorio() {
    Articulos articulo = new Articulos();
    servicioArticulo.registrarArticulo(articulo);
    verify(repositorioArticuloMock, times(1)).guardar(articulo);
  }

  @Test
  public void buscarPorIdDeberiaRetornarArticulo() {
    Articulos esperado = new Articulos();
    when(repositorioArticuloMock.buscarPorId(1L)).thenReturn(esperado);

    Articulos obtenido = servicioArticulo.buscarPorId(1L);

    assertThat(obtenido, equalTo(esperado));
  }

  @Test
  public void buscarPorNombreDeberiaRetornarListaDeArticulos() {
    List<Articulos> esperados = Collections.singletonList(new Articulos());
    when(repositorioArticuloMock.buscarPorNombre("Test")).thenReturn(esperados);

    List<Articulos> obtenidos = servicioArticulo.buscarPorNombre("Test");

    assertThat(obtenidos, equalTo(esperados));
  }

  @Test
  public void obtenerStockAgrupadoDeberiaRetornarListaDeStock() {
    List<StockArticuloDto> esperados = Collections.singletonList(new StockArticuloDto());
    when(repositorioArticuloMock.obtenerStockAgrupadoPorNombre()).thenReturn(esperados);

    List<StockArticuloDto> obtenidos = servicioArticulo.obtenerStockAgrupado();

    assertThat(obtenidos, equalTo(esperados));
  }

  @Test
  public void descontarStockDeberiaRestarCantidadYGuardarArticulo() {
    // preparacion
    Articulos articulo = new Articulos();
    articulo.setId(1L);
    articulo.setCantidad(10.0);
    when(repositorioArticuloMock.buscarPorId(1L)).thenReturn(articulo);

    // ejecucion
    servicioArticulo.descontarStock(1L, 3.5);

    // validacion
    assertThat(articulo.getCantidad(), equalTo(6.5));
    verify(repositorioArticuloMock, times(1)).guardar(articulo);
  }

  @Test
  public void descontarStockDeberiaLanzarExcepcionSiArticuloNoExiste() {
    // preparacion
    when(repositorioArticuloMock.buscarPorId(1L)).thenReturn(null);

    // ejecucion & validacion
    try {
      servicioArticulo.descontarStock(1L, 5.0);
      org.junit.jupiter.api.Assertions.fail("Debería haber lanzado IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), equalTo("El artículo no existe."));
    }
  }

  @Test
  public void descontarStockDeberiaLanzarExcepcionSiCantidadEsNulaOMenorIgualACero() {
    // ejecucion & validacion (cantidad nula)
    try {
      servicioArticulo.descontarStock(1L, null);
      org.junit.jupiter.api.Assertions.fail("Debería haber lanzado IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), equalTo("La cantidad a descontar debe ser mayor a cero."));
    }

    // ejecucion & validacion (cantidad cero o negativa)
    try {
      servicioArticulo.descontarStock(1L, -1.0);
      org.junit.jupiter.api.Assertions.fail("Debería haber lanzado IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), equalTo("La cantidad a descontar debe ser mayor a cero."));
    }
  }

  @Test
  public void descontarStockDeberiaLanzarExcepcionSiStockEsInsuficiente() {
    // preparacion
    Articulos articulo = new Articulos();
    articulo.setId(1L);
    articulo.setCantidad(4.0);
    when(repositorioArticuloMock.buscarPorId(1L)).thenReturn(articulo);

    // ejecucion & validacion
    try {
      servicioArticulo.descontarStock(1L, 4.5);
      org.junit.jupiter.api.Assertions.fail("Debería haber lanzado IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertThat(
        e.getMessage(),
        equalTo("La cantidad a descontar no puede superar al stock disponible.")
      );
    }
  }

  @Test
  public void obtenerNotificacionesVencimientoDeberiaRetornarSoloArticulosAlVencerEnDiezDiasOMenos() {
    // preparacion
    java.time.OffsetDateTime ahora = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);

    Articulos a1 = new Articulos();
    a1.setNombre("Articulo 1");
    a1.setCantidad(5.0);
    a1.setFechaDeVencimiento(ahora.plusDays(2)); // Vence en 2 dias (ALTA)

    Articulos a2 = new Articulos();
    a2.setNombre("Articulo 2");
    a2.setCantidad(3.0);
    a2.setFechaDeVencimiento(ahora.plusDays(6)); // Vence en 6 dias (MEDIA)

    Articulos a3 = new Articulos();
    a3.setNombre("Articulo 3");
    a3.setCantidad(1.0);
    a3.setFechaDeVencimiento(ahora.plusDays(9)); // Vence en 9 dias (BAJA)

    Articulos a4 = new Articulos();
    a4.setNombre("Articulo 4");
    a4.setCantidad(10.0);
    a4.setFechaDeVencimiento(ahora.plusDays(12)); // Vence en 12 dias (no deberia retornar)

    Articulos a5 = new Articulos();
    a5.setNombre("Articulo 5");
    a5.setCantidad(1.0);
    a5.setFechaDeVencimiento(ahora.minusDays(1)); // Vencido hace 1 dia (ALTA)

    List<Articulos> todos = java.util.List.of(a1, a2, a3, a4, a5);
    when(repositorioArticuloMock.obtenerTodos()).thenReturn(todos);

    // ejecucion
    List<com.tallerwebi.presentacion.dto.NotificacionVencimientoDto> resultado =
      servicioArticulo.obtenerNotificacionesVencimiento();

    // validacion
    assertThat(resultado, hasSize(4));
    // ordenado por dias restantes: a5 (dias < 0), a1 (2 dias), a2 (6 dias), a3 (9 dias)
    assertThat(resultado.get(0).getArticulo().getNombre(), equalTo("Articulo 5"));
    assertThat(resultado.get(0).getNivelUrgencia(), equalTo("ALTA"));
    assertThat(resultado.get(1).getArticulo().getNombre(), equalTo("Articulo 1"));
    assertThat(resultado.get(1).getNivelUrgencia(), equalTo("ALTA"));
    assertThat(resultado.get(2).getArticulo().getNombre(), equalTo("Articulo 2"));
    assertThat(resultado.get(2).getNivelUrgencia(), equalTo("MEDIA"));
    assertThat(resultado.get(3).getArticulo().getNombre(), equalTo("Articulo 3"));
    assertThat(resultado.get(3).getNivelUrgencia(), equalTo("BAJA"));
  }

  @Test
  public void obtenerNotificacionesVencimientoDeberiaIgnorarArticulosSinStock() {
    // preparacion
    java.time.OffsetDateTime ahora = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);

    Articulos a1 = new Articulos();
    a1.setNombre("Articulo 1");
    a1.setCantidad(0.0); // Sin stock
    a1.setFechaDeVencimiento(ahora.plusDays(2));

    Articulos a2 = new Articulos();
    a2.setNombre("Articulo 2");
    a2.setCantidad(null); // Cantidad nula
    a2.setFechaDeVencimiento(ahora.plusDays(2));

    Articulos a3 = new Articulos();
    a3.setNombre("Articulo 3");
    a3.setCantidad(5.0); // Con stock
    a3.setFechaDeVencimiento(ahora.plusDays(2));

    List<Articulos> todos = java.util.List.of(a1, a2, a3);
    when(repositorioArticuloMock.obtenerTodos()).thenReturn(todos);

    // ejecucion
    List<com.tallerwebi.presentacion.dto.NotificacionVencimientoDto> resultado =
      servicioArticulo.obtenerNotificacionesVencimiento();

    // validacion
    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getArticulo().getNombre(), equalTo("Articulo 3"));
  }
}
