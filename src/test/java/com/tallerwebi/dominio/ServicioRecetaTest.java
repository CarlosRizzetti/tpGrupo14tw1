package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.*;
import com.tallerwebi.dominio.interfaces.RepositorioArticulo;
import com.tallerwebi.dominio.interfaces.RepositorioReceta;
import com.tallerwebi.dominio.services.ServicioRecetaImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRecetaTest {

  private RepositorioReceta repositorioRecetaMock;
  private RepositorioArticulo repositorioArticuloMock;
  private ServicioRecetaImpl servicioReceta;

  @BeforeEach
  public void init() {
    repositorioRecetaMock = mock(RepositorioReceta.class);
    repositorioArticuloMock = mock(RepositorioArticulo.class);
    servicioReceta = new ServicioRecetaImpl(repositorioRecetaMock, repositorioArticuloMock);
  }

  @Test
  public void guardarRecetaNuevaDeberiaCrearNuevaReceta() {
    Producto producto = new Producto();
    when(repositorioRecetaMock.buscarPorProducto(producto)).thenReturn(null);

    Articulos articulo = new Articulos();
    when(repositorioArticuloMock.buscarPorId(1L)).thenReturn(articulo);

    List<Long> articulosIds = Arrays.asList(1L);
    List<Double> cantidades = Arrays.asList(2.0);

    servicioReceta.guardarReceta(producto, articulosIds, cantidades);

    verify(repositorioRecetaMock, times(1)).guardar(any(Receta.class));
  }

  @Test
  public void buscarPorProductoDeberiaRetornarReceta() {
    Producto producto = new Producto();
    Receta recetaEsperada = new Receta();
    when(repositorioRecetaMock.buscarPorProducto(producto)).thenReturn(recetaEsperada);

    Receta resultado = servicioReceta.buscarPorProducto(producto);

    assertEquals(recetaEsperada, resultado);
  }

  @Test
  public void obtenerTodasDeberiaRetornarListaDeRecetas() {
    List<Receta> lista = new ArrayList<>();
    when(repositorioRecetaMock.obtenerTodas()).thenReturn(lista);

    List<Receta> resultado = servicioReceta.obtenerTodas();

    assertEquals(lista, resultado);
  }
}
