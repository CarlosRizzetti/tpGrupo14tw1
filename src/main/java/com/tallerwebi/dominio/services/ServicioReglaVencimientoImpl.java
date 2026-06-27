package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.*;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioReglaVencimiento")
@Transactional
public class ServicioReglaVencimientoImpl implements ServicioReglaVencimiento {

  private final RepositorioReglaVencimiento repositorioReglaVencimiento;
  private final RepositorioTimer repositorioTimer;
  private final ServicioProducto servicioProducto;
  private final ServicioControlStock servicioControlStock;
  private final Clock clock;
  private final ServicioImpresion servicioImpresion;

  @Autowired
  public ServicioReglaVencimientoImpl(
    RepositorioReglaVencimiento repositorioReglaVencimiento,
    RepositorioTimer repositorioTimer,
    ServicioProducto servicioProducto,
    ServicioControlStock servicioControlStock,
    Clock clock,
    ServicioImpresion servicioImpresion
  ) {
    this.repositorioReglaVencimiento = repositorioReglaVencimiento;
    this.repositorioTimer = repositorioTimer;
    this.servicioProducto = servicioProducto;
    this.servicioControlStock = servicioControlStock;
    this.clock = clock;
    this.servicioImpresion = servicioImpresion;
  }

  @Override
  public void guardarReglaVencimiento(ReglaVencimiento reglaVencimiento) {
    validar(reglaVencimiento);
    repositorioReglaVencimiento.guardar(reglaVencimiento);
  }

  @Override
  public ReglaVencimiento obtenerReglaVencimientoPorId(Long id) {
    return repositorioReglaVencimiento.obtenerReglaVencimientoPorId(id);
  }

  @Override
  public Timer generarVencimiento(
    Producto producto,
    Categoria categoria,
    Long reglaId,
    Integer offsetMinutos,
    Integer cantidadUsada,
    Usuario usuario
  ) {
    ReglaVencimiento regla = repositorioReglaVencimiento.obtenerReglaVencimientoPorId(reglaId);
    if (regla == null) {
      throw new IllegalArgumentException("El producto no tiene regla de vencimiento");
    }

    validarCantidadUsada(cantidadUsada);
    servicioProducto.descontarStock(producto, cantidadUsada);

    OffsetDateTime fechaElaboracion = obtenerFechaDeElaboracion(offsetMinutos);
    OffsetDateTime vencimiento = obtenerFechaVencimiento(fechaElaboracion, regla);
    OffsetDateTime descongelamiento = obtenerFechaDeDescongelamiento(fechaElaboracion, regla);

    Timer timer = new Timer(
      fechaElaboracion,
      vencimiento,
      descongelamiento,
      producto,
      categoria,
      regla,
      cantidadUsada,
      usuario
    );
    repositorioTimer.guardar(timer);

    servicioControlStock.registrarMovimiento(
      producto,
      timer,
      cantidadUsada,
      TipoMovimientoStock.EGRESO
    );

    servicioImpresion.imprimirTicketVencimiento(
      producto,
      regla,
      fechaElaboracion,
      vencimiento,
      descongelamiento
    );

    return timer;
  }

  private void validarCantidadUsada(Integer cantidadUsada) {
    if (cantidadUsada == null || cantidadUsada <= 0) {
      throw new IllegalArgumentException("La cantidad a utilizar debe ser mayor a 0");
    }
  }

  private OffsetDateTime obtenerFechaDeDescongelamiento(
    OffsetDateTime fechaElaboracion,
    ReglaVencimiento regla
  ) {
    if (!regla.getTieneDescongelamiento()) return null;
    if (regla.getDescongelamientoMinutos() == null) return null;
    return fechaElaboracion.plusMinutes(regla.getDescongelamientoMinutos());
  }

  private OffsetDateTime obtenerFechaVencimiento(
    OffsetDateTime fechaElaboracion,
    ReglaVencimiento regla
  ) {
    return fechaElaboracion.plusMinutes(regla.getDuracionMinutos());
  }

  private OffsetDateTime obtenerFechaDeElaboracion(Integer offsetMinutos) {
    OffsetDateTime fechaElaboracion = OffsetDateTime.now(clock);
    if (offsetMinutos != null && offsetMinutos > 0) {
      fechaElaboracion.minusMinutes(offsetMinutos);
    }
    return fechaElaboracion;
  }

  private void validar(ReglaVencimiento regla) {
    validarUbicacion(regla);
    validarDuracion(regla);
    validarDescongelamiento(regla);
  }

  private void validarUbicacion(ReglaVencimiento regla) {
    if (regla.getUbicacion() == null || regla.getUbicacion().isBlank()) {
      throw new IllegalArgumentException("La ubicación es obligatoria");
    }
  }

  private void validarDuracion(ReglaVencimiento regla) {
    if (regla.getDuracionMinutos() == null || regla.getDuracionMinutos() <= 0) {
      throw new IllegalArgumentException("La duración debe ser mayor a 0");
    }
  }

  private void validarDescongelamiento(ReglaVencimiento regla) {
    if (
      Boolean.TRUE.equals(regla.getTieneDescongelamiento()) &&
      (regla.getDescongelamientoMinutos() == null || regla.getDescongelamientoMinutos() <= 0)
    ) {
      throw new IllegalArgumentException("Los minutos de descongelamiento son obligatorios");
    }
  }
}
