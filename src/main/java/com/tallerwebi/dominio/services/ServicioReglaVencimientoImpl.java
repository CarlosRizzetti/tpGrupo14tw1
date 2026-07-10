package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.RepositorioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioControlStock;
import com.tallerwebi.dominio.interfaces.ServicioImpresion;
import com.tallerwebi.dominio.interfaces.ServicioProduccion;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.utils.ImpresionHelper;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioReglaVencimiento")
@Transactional
public class ServicioReglaVencimientoImpl implements ServicioReglaVencimiento {

  private static final Logger log = LoggerFactory.getLogger(ServicioReglaVencimientoImpl.class);
  private final RepositorioReglaVencimiento repositorioReglaVencimiento;
  private final RepositorioTimer repositorioTimer;
  private final ServicioProducto servicioProducto;
  private final ServicioControlStock servicioControlStock;
  private final Clock clock;
  private final ServicioImpresion servicioImpresion;
  private final ServicioProduccion servicioProduccion;

  @Autowired
  public ServicioReglaVencimientoImpl(
    RepositorioReglaVencimiento repositorioReglaVencimiento,
    RepositorioTimer repositorioTimer,
    ServicioProducto servicioProducto,
    ServicioControlStock servicioControlStock,
    Clock clock,
    ServicioImpresion servicioImpresion,
    ServicioProduccion servicioProduccion
  ) {
    this.repositorioReglaVencimiento = repositorioReglaVencimiento;
    this.repositorioTimer = repositorioTimer;
    this.servicioProducto = servicioProducto;
    this.servicioControlStock = servicioControlStock;
    this.clock = clock;
    this.servicioImpresion = servicioImpresion;
    this.servicioProduccion = servicioProduccion;
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
    ReglaVencimiento regla = obtenerReglaValidada(reglaId);
    validarCantidadUsada(cantidadUsada);
    servicioProducto.descontarStock(producto, cantidadUsada);

    Timer timer = crearYGuardarTimer(
      producto,
      categoria,
      regla,
      offsetMinutos,
      cantidadUsada,
      usuario
    );
    servicioControlStock.registrarMovimiento(
      producto,
      timer,
      cantidadUsada,
      TipoMovimientoStock.EGRESO
    );

    servicioProduccion.procesarProduccion(producto, timer, cantidadUsada);

    ImpresionHelper.intentarImpresionDeVencimiento(timer, servicioImpresion);

    return timer;
  }

  private ReglaVencimiento obtenerReglaValidada(Long reglaId) {
    ReglaVencimiento regla = repositorioReglaVencimiento.obtenerReglaVencimientoPorId(reglaId);
    if (regla == null) {
      throw new IllegalArgumentException("El producto no tiene regla de vencimiento");
    }
    return regla;
  }

  private Timer crearYGuardarTimer(
    Producto producto,
    Categoria categoria,
    ReglaVencimiento regla,
    Integer offsetMinutos,
    Integer cantidadUsada,
    Usuario usuario
  ) {
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
