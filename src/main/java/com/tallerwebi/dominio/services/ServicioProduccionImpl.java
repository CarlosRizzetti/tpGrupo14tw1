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

  private void validarStockSuficiente(Receta receta, Integer cantidadAProducir) {
    for (RecetaDetalle detalle : receta.getIngredientes()) {
      Articulos articulo = detalle.getArticulo();
      Double cantidadRequerida = detalle.getCantidad() * cantidadAProducir;
      Double cantidadDisponible = articulo.getCantidad() != null ? articulo.getCantidad() : 0.0;

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

  private void descontarStockYGenerarTrazabilidad(
    Producto producto,
    Timer timer,
    Receta receta,
    Integer cantidadAProducir
  ) {
    Trazabilidad trazabilidad = new Trazabilidad();
    trazabilidad.setFechaGeneracion(OffsetDateTime.now());
    trazabilidad.setProducto(producto);
    trazabilidad.setTimer(timer);

    List<TrazabilidadDetalle> detallesTrazabilidad = new ArrayList<>();

    for (RecetaDetalle detalle : receta.getIngredientes()) {
      Articulos articulo = detalle.getArticulo();
      Double cantidadRequerida = detalle.getCantidad() * cantidadAProducir;

      Double cantidadDisponible = articulo.getCantidad() != null ? articulo.getCantidad() : 0.0;
      articulo.setCantidad(cantidadDisponible - cantidadRequerida);
      repositorioArticulo.guardar(articulo);

      TrazabilidadDetalle trazabilidadDetalle = new TrazabilidadDetalle();
      trazabilidadDetalle.setArticulo(articulo);
      trazabilidadDetalle.setCantidadUsada(cantidadRequerida);
      trazabilidadDetalle.setTrazabilidad(trazabilidad);

      detallesTrazabilidad.add(trazabilidadDetalle);
    }

    trazabilidad.setArticulosUsados(detallesTrazabilidad);
    repositorioTrazabilidad.guardar(trazabilidad);
  }

  @Override
  public List<Trazabilidad> obtenerTrazabilidadCompleta() {
    return repositorioTrazabilidad.obtenerTodas();
  }
}
