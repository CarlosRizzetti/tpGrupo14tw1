package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.*;
import com.tallerwebi.dominio.excepcion.SinStockSuficienteException;
import com.tallerwebi.dominio.interfaces.RepositorioArticulo;
import com.tallerwebi.dominio.interfaces.RepositorioReceta;
import com.tallerwebi.dominio.interfaces.RepositorioTrazabilidad;
import com.tallerwebi.dominio.services.ServicioProduccionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioProduccionTest {

  private RepositorioReceta repositorioRecetaMock;
  private RepositorioArticulo repositorioArticuloMock;
  private RepositorioTrazabilidad repositorioTrazabilidadMock;
  private ServicioProduccionImpl servicioProduccion;

  @BeforeEach
  public void init() {
    repositorioRecetaMock = mock(RepositorioReceta.class);
    repositorioArticuloMock = mock(RepositorioArticulo.class);
    repositorioTrazabilidadMock = mock(RepositorioTrazabilidad.class);
    servicioProduccion =
      new ServicioProduccionImpl(
        repositorioRecetaMock,
        repositorioArticuloMock,
        repositorioTrazabilidadMock
      );
  }

  @Test
  public void procesarProduccionConRecetaNulaNoDeberiaLanzarExcepcion() {
    Producto producto = new Producto();
    Timer timer = new Timer();
    when(repositorioRecetaMock.buscarPorProducto(producto)).thenReturn(null);

    servicioProduccion.procesarProduccion(producto, timer, 1);

    verify(repositorioArticuloMock, never()).guardar(any());
    verify(repositorioTrazabilidadMock, never()).guardar(any());
  }

  @Test
  public void procesarProduccionSinStockSuficienteDeberiaLanzarExcepcion() {
    Producto producto = new Producto();
    Timer timer = new Timer();
    Receta receta = new Receta();
    receta.setProducto(producto);

    Articulos articulo = new Articulos();
    articulo.setCantidad(5.0);
    articulo.setNombre("Pan");

    RecetaDetalle detalle = new RecetaDetalle();
    detalle.setArticulo(articulo);
    detalle.setCantidad(10.0); // Se necesitan 10 pero hay 5

    List<RecetaDetalle> ingredientes = new ArrayList<>();
    ingredientes.add(detalle);
    receta.setIngredientes(ingredientes);

    when(repositorioRecetaMock.buscarPorProducto(producto)).thenReturn(receta);

    assertThrows(
      SinStockSuficienteException.class,
      () -> {
        servicioProduccion.procesarProduccion(producto, timer, 1);
      }
    );

    verify(repositorioArticuloMock, never()).guardar(any());
  }

  @Test
  public void procesarProduccionConStockSuficienteDeberiaDescontarYGuardarTrazabilidad() {
    Producto producto = new Producto();
    Timer timer = new Timer();
    Receta receta = new Receta();
    receta.setProducto(producto);

    Articulos articulo = new Articulos();
    articulo.setCantidad(15.0);
    articulo.setNombre("Pan");

    RecetaDetalle detalle = new RecetaDetalle();
    detalle.setArticulo(articulo);
    detalle.setCantidad(10.0);

    List<RecetaDetalle> ingredientes = new ArrayList<>();
    ingredientes.add(detalle);
    receta.setIngredientes(ingredientes);

    when(repositorioRecetaMock.buscarPorProducto(producto)).thenReturn(receta);

    servicioProduccion.procesarProduccion(producto, timer, 1);

    verify(repositorioArticuloMock, times(1)).guardar(articulo);
    verify(repositorioTrazabilidadMock, times(1)).guardar(any(Trazabilidad.class));
  }
}
