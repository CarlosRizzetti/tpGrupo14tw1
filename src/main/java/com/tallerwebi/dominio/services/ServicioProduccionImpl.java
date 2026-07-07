package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Receta;
import com.tallerwebi.dominio.entity.RecetaDetalle;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Trazabilidad;
import com.tallerwebi.dominio.entity.TrazabilidadDetalle;
import com.tallerwebi.dominio.excepcion.SinStockSuficienteException;
import com.tallerwebi.dominio.interfaces.RepositorioArticulo;
import com.tallerwebi.dominio.interfaces.RepositorioReceta;
import com.tallerwebi.dominio.interfaces.RepositorioTrazabilidad;
import com.tallerwebi.dominio.interfaces.ServicioProduccion;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioProduccion")
@Transactional
public class ServicioProduccionImpl implements ServicioProduccion {

  private final RepositorioReceta repositorioReceta;
  private final RepositorioArticulo repositorioArticulo;
  private final RepositorioTrazabilidad repositorioTrazabilidad;

  @Autowired
  public ServicioProduccionImpl(
    RepositorioReceta repositorioReceta,
    RepositorioArticulo repositorioArticulo,
    RepositorioTrazabilidad repositorioTrazabilidad
  ) {
    this.repositorioReceta = repositorioReceta;
    this.repositorioArticulo = repositorioArticulo;
    this.repositorioTrazabilidad = repositorioTrazabilidad;
  }

  @Override
  public boolean procesarProduccion(Producto producto, Timer timer, Integer cantidadAProducir) {
    if (!tieneReceta(producto)) {
      return false; // No hay receta o ingredientes, entonces no se descuenta nada.
    }

    Receta receta = repositorioReceta.buscarPorProducto(producto);
    validarStockSuficiente(receta, cantidadAProducir);
    descontarStockYGenerarTrazabilidad(producto, timer, receta, cantidadAProducir);
    return true;
  }

  @Override
  public boolean tieneReceta(Producto producto) {
    Receta receta = repositorioReceta.buscarPorProducto(producto);
    return (
      receta != null && receta.getIngredientes() != null && !receta.getIngredientes().isEmpty()
    );
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void validarStockSuficiente(Receta receta, Integer cantidadAProducir) {
    List<com.tallerwebi.presentacion.dto.StockArticuloDto> stockAgrupado = obtenerStockAgrupado();

    for (RecetaDetalle detalle : receta.getIngredientes()) {
      Articulos articulo = detalle.getArticulo();
      Double cantidadRequerida = detalle.getCantidad() * cantidadAProducir;

      Double cantidadDisponible = stockAgrupado
        .stream()
        .filter(dto -> dto.getNombre() != null && dto.getNombre().equals(articulo.getNombre()))
        .map(com.tallerwebi.presentacion.dto.StockArticuloDto::getStock)
        .findFirst()
        .orElse(0.0);

      if (cantidadDisponible < cantidadRequerida) {
        throw new SinStockSuficienteException(
          "No hay stock suficiente para el ingrediente: " +
          articulo.getNombre() +
          ". Se necesitan " +
          cantidadRequerida +
          " pero hay " +
          cantidadDisponible
        );
      }
    }
  }

  @Override
  public List<com.tallerwebi.presentacion.dto.StockArticuloDto> obtenerStockAgrupado() {
    return repositorioArticulo.obtenerStockAgrupadoPorNombre();
  }

  private void descontarStockYGenerarTrazabilidad(
    Producto producto,
    Timer timer,
    Receta receta,
    Integer cantidadAProducir
  ) {
    Trazabilidad trazabilidad = generarTrazabilidad(producto, timer);
    List<TrazabilidadDetalle> detalles = descontarStock(receta, cantidadAProducir, trazabilidad);
    trazabilidad.setArticulosUsados(detalles);
    repositorioTrazabilidad.guardar(trazabilidad);
  }

  private Trazabilidad generarTrazabilidad(Producto producto, Timer timer) {
    Trazabilidad trazabilidad = new Trazabilidad();
    trazabilidad.setFechaGeneracion(OffsetDateTime.now());
    trazabilidad.setProducto(producto);
    trazabilidad.setTimer(timer);
    return trazabilidad;
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private List<TrazabilidadDetalle> descontarStock(
    Receta receta,
    Integer cantidadAProducir,
    Trazabilidad trazabilidad
  ) {
    List<TrazabilidadDetalle> detalles = new ArrayList<>();
    for (RecetaDetalle detalle : receta.getIngredientes()) {
      detalles.addAll(descontarIngrediente(detalle, cantidadAProducir, trazabilidad));
    }
    return detalles;
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private List<TrazabilidadDetalle> descontarIngrediente(
    RecetaDetalle detalle,
    Integer cantidadAProducir,
    Trazabilidad trazabilidad
  ) {
    List<TrazabilidadDetalle> detalles = new ArrayList<>();
    Articulos articuloReceta = detalle.getArticulo();
    Double cantidadRestante = detalle.getCantidad() * cantidadAProducir;

    List<Articulos> articulosMismoNombre = repositorioArticulo.buscarPorNombre(
      articuloReceta.getNombre()
    );

    for (Articulos articulo : articulosMismoNombre) {
      if (
        articulo.getNombre() == null || !articulo.getNombre().equals(articuloReceta.getNombre())
      ) {
        continue;
      }

      if (cantidadRestante <= 0) {
        break;
      }

      Double cantidadDisponible = articulo.getCantidad() != null ? articulo.getCantidad() : 0.0;

      if (cantidadDisponible > 0) {
        Double cantidadADescontar = Math.min(cantidadDisponible, cantidadRestante);
        articulo.setCantidad(cantidadDisponible - cantidadADescontar);
        repositorioArticulo.guardar(articulo);

        TrazabilidadDetalle td = new TrazabilidadDetalle();
        td.setArticulo(articulo);
        td.setCantidadUsada(cantidadADescontar);
        td.setTrazabilidad(trazabilidad);
        detalles.add(td);

        cantidadRestante -= cantidadADescontar;
      }
    }

    if (cantidadRestante > 0) {
      throw new SinStockSuficienteException(
        "No hay stock disponible suficiente al descontar para el ingrediente: " +
        articuloReceta.getNombre()
      );
    }
    return detalles;
  }

  @Override
  public List<Trazabilidad> obtenerTrazabilidadCompleta() {
    return repositorioTrazabilidad.obtenerTodas();
  }
}
