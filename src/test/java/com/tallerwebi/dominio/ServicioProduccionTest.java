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

    List<com.tallerwebi.presentacion.dto.StockArticuloDto> stockAgrupado = new ArrayList<>();
    stockAgrupado.add(new com.tallerwebi.presentacion.dto.StockArticuloDto("Pan", 5.0));
    when(repositorioArticuloMock.obtenerStockAgrupadoPorNombre()).thenReturn(stockAgrupado);

    List<Articulos> articulosMismoNombre = new ArrayList<>();
    articulosMismoNombre.add(articulo);
    when(repositorioArticuloMock.buscarPorNombre("Pan")).thenReturn(articulosMismoNombre);

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

    List<com.tallerwebi.presentacion.dto.StockArticuloDto> stockAgrupado = new ArrayList<>();
    stockAgrupado.add(new com.tallerwebi.presentacion.dto.StockArticuloDto("Pan", 15.0));
    when(repositorioArticuloMock.obtenerStockAgrupadoPorNombre()).thenReturn(stockAgrupado);

    List<Articulos> articulosMismoNombre = new ArrayList<>();
    articulosMismoNombre.add(articulo);
    when(repositorioArticuloMock.buscarPorNombre("Pan")).thenReturn(articulosMismoNombre);

    when(repositorioRecetaMock.buscarPorProducto(producto)).thenReturn(receta);

    servicioProduccion.procesarProduccion(producto, timer, 1);

    verify(repositorioArticuloMock, times(1)).guardar(articulo);
    verify(repositorioTrazabilidadMock, times(1)).guardar(any(Trazabilidad.class));
  }

  @Test
  public void procesarProduccionConStockDistribuidoDeberiaDescontarDeVariosArticulos() {
    Producto producto = new Producto();
    Timer timer = new Timer();
    Receta receta = new Receta();
    receta.setProducto(producto);

    RecetaDetalle detalle = new RecetaDetalle();
    Articulos articuloReceta = new Articulos();
    articuloReceta.setNombre("Pan");
    detalle.setArticulo(articuloReceta);
    detalle.setCantidad(10.0);

    List<RecetaDetalle> ingredientes = new ArrayList<>();
    ingredientes.add(detalle);
    receta.setIngredientes(ingredientes);

    List<com.tallerwebi.presentacion.dto.StockArticuloDto> stockAgrupado = new ArrayList<>();
    stockAgrupado.add(new com.tallerwebi.presentacion.dto.StockArticuloDto("Pan", 15.0));
    when(repositorioArticuloMock.obtenerStockAgrupadoPorNombre()).thenReturn(stockAgrupado);

    Articulos articulo1 = new Articulos();
    articulo1.setNombre("Pan");
    articulo1.setCantidad(6.0);

    Articulos articulo2 = new Articulos();
    articulo2.setNombre("Pan");
    articulo2.setCantidad(9.0);

    List<Articulos> articulosMismoNombre = new ArrayList<>();
    articulosMismoNombre.add(articulo1);
    articulosMismoNombre.add(articulo2);
    when(repositorioArticuloMock.buscarPorNombre("Pan")).thenReturn(articulosMismoNombre);

    when(repositorioRecetaMock.buscarPorProducto(producto)).thenReturn(receta);

    servicioProduccion.procesarProduccion(producto, timer, 1);

    verify(repositorioArticuloMock, times(1)).guardar(articulo1);
    verify(repositorioArticuloMock, times(1)).guardar(articulo2);
    verify(repositorioTrazabilidadMock, times(1)).guardar(any(Trazabilidad.class));

    org.junit.jupiter.api.Assertions.assertEquals(0.0, articulo1.getCantidad());
    org.junit.jupiter.api.Assertions.assertEquals(5.0, articulo2.getCantidad());
  }

  @Test
  public void procesarProduccionFiltraArticulosPorNombreExactoYDescuentaCorrectamente() {
    Producto producto = new Producto();
    Timer timer = new Timer();
    Receta receta = new Receta();
    receta.setProducto(producto);

    RecetaDetalle detalle = new RecetaDetalle();
    Articulos articuloReceta = new Articulos();
    articuloReceta.setNombre("Pan");
    detalle.setArticulo(articuloReceta);
    detalle.setCantidad(10.0);

    List<RecetaDetalle> ingredientes = new ArrayList<>();
    ingredientes.add(detalle);
    receta.setIngredientes(ingredientes);

    List<com.tallerwebi.presentacion.dto.StockArticuloDto> stockAgrupado = new ArrayList<>();
    stockAgrupado.add(new com.tallerwebi.presentacion.dto.StockArticuloDto("Pan", 15.0));
    when(repositorioArticuloMock.obtenerStockAgrupadoPorNombre()).thenReturn(stockAgrupado);

    Articulos articuloFalso = new Articulos();
    articuloFalso.setNombre("Pancho");
    articuloFalso.setCantidad(20.0);

    Articulos articuloCorrecto = new Articulos();
    articuloCorrecto.setNombre("Pan");
    articuloCorrecto.setCantidad(15.0);

    List<Articulos> articulosMismoNombre = new ArrayList<>();
    articulosMismoNombre.add(articuloFalso);
    articulosMismoNombre.add(articuloCorrecto);
    when(repositorioArticuloMock.buscarPorNombre("Pan")).thenReturn(articulosMismoNombre);

    when(repositorioRecetaMock.buscarPorProducto(producto)).thenReturn(receta);

    servicioProduccion.procesarProduccion(producto, timer, 1);

    verify(repositorioArticuloMock, never()).guardar(articuloFalso);
    verify(repositorioArticuloMock, times(1)).guardar(articuloCorrecto);

    org.junit.jupiter.api.Assertions.assertEquals(20.0, articuloFalso.getCantidad());
    org.junit.jupiter.api.Assertions.assertEquals(5.0, articuloCorrecto.getCantidad());
  }
}
