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
}
