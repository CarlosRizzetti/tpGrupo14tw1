package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.interfaces.RepositorioArticulo;
import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioArticuloImpl implements ServicioArticulo {

  private static final int URGENCIA_ALTA_DIAS = 3;
  private static final int URGENCIA_MEDIA_DIAS = 7;
  private static final int URGENCIA_BAJA_DIAS = 10;

  private final RepositorioArticulo repositorioArticulo;

  @Autowired
  public ServicioArticuloImpl(RepositorioArticulo repositorioArticulo) {
    this.repositorioArticulo = repositorioArticulo;
  }

  @Override
  public List<Articulos> obtenerTodosLosArticulos() {
    return repositorioArticulo.obtenerTodos();
  }

  @Override
  public void registrarArticulo(Articulos articulo) {
    repositorioArticulo.guardar(articulo);
  }

  @Override
  public Articulos buscarPorId(Long id) {
    return repositorioArticulo.buscarPorId(id);
  }

  @Override
  public List<Articulos> buscarPorNombre(String nombre) {
    return repositorioArticulo.buscarPorNombre(nombre);
  }

  @Override
  public List<com.tallerwebi.presentacion.dto.StockArticuloDto> obtenerStockAgrupado() {
    return repositorioArticulo.obtenerStockAgrupadoPorNombre();
  }

  @Override
  public void descontarStock(Long id, Double cantidadADescontar) {
    if (cantidadADescontar == null || cantidadADescontar <= 0) {
      throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
    }
    Articulos articulo = repositorioArticulo.buscarPorId(id);
    if (articulo == null) {
      throw new IllegalArgumentException("El artículo no existe.");
    }
    Double cantidadActual = articulo.getCantidad() != null ? articulo.getCantidad() : 0.0;
    if (cantidadActual < cantidadADescontar) {
      throw new IllegalArgumentException(
        "La cantidad a descontar no puede superar al stock disponible."
      );
    }
    articulo.setCantidad(cantidadActual - cantidadADescontar);
    repositorioArticulo.guardar(articulo);
  }

  @Override
  public List<
    com.tallerwebi.presentacion.dto.NotificacionVencimientoDto
  > obtenerNotificacionesVencimiento() {
    List<com.tallerwebi.presentacion.dto.NotificacionVencimientoDto> notificaciones =
      new java.util.ArrayList<>();
    List<Articulos> todos = repositorioArticulo.obtenerTodos();

    for (Articulos art : todos) {
      if (art.getCantidad() == null || art.getCantidad() <= 0) {
        continue;
      }
      if (art.getFechaDeVencimiento() != null) {
        java.time.LocalDate vencimiento = art.getFechaDeVencimiento().toLocalDate();
        long dias = java.time.temporal.ChronoUnit.DAYS.between(
          java.time.LocalDate.now(),
          vencimiento
        );
        if (dias <= URGENCIA_BAJA_DIAS) {
          String urgencia;
          if (dias <= URGENCIA_ALTA_DIAS) {
            urgencia = "ALTA";
          } else if (dias <= URGENCIA_MEDIA_DIAS) {
            urgencia = "MEDIA";
          } else {
            urgencia = "BAJA";
          }
          notificaciones.add(
            new com.tallerwebi.presentacion.dto.NotificacionVencimientoDto(art, dias, urgencia)
          );
        }
      }
    }

    notificaciones.sort(
      java.util.Comparator.comparingLong(
        com.tallerwebi.presentacion.dto.NotificacionVencimientoDto::getDiasRestantes
      )
    );
    return notificaciones;
  }
}
