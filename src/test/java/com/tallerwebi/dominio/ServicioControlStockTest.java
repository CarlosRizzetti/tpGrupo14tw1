package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.ControlStock;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.RepositorioControlStock;
import com.tallerwebi.dominio.services.ServicioControlStockImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioControlStockTest {

  private ServicioControlStockImpl servicioControlStock;
  private RepositorioControlStock repositorioControlStockMock;
  private Clock clock;

  @BeforeEach
  public void init() {
    clock = Clock.fixed(Instant.now(), ZoneOffset.ofHours(-3));
    repositorioControlStockMock = mock(RepositorioControlStock.class);
    servicioControlStock = new ServicioControlStockImpl(repositorioControlStockMock, clock);
  }

  @Test
  @DisplayName("HAP-01 | registrarMovimiento | Guarda el movimiento en el repositorio")
  public void registrarMovimientoDeberiaGuardarEnRepositorio() {
    servicioControlStock.registrarMovimiento(
      productoConStock(10),
      new Timer(),
      3,
      TipoMovimientoStock.EGRESO
    );

    verify(repositorioControlStockMock, times(1)).guardar(any(ControlStock.class));
  }

  @Test
  @DisplayName("HAP-02 | registrarMovimiento | El movimiento guarda el producto correcto")
  public void registrarMovimientoDeberiaAsociarElProductoCorrecto() {
    Producto producto = productoConStock(10);
    producto.setId(5L);

    servicioControlStock.registrarMovimiento(producto, new Timer(), 2, TipoMovimientoStock.EGRESO);

    ArgumentCaptor<ControlStock> captor = ArgumentCaptor.forClass(ControlStock.class);
    verify(repositorioControlStockMock).guardar(captor.capture());
    assertEquals(producto, captor.getValue().getProducto());
  }

  @Test
  @DisplayName("HAP-03 | registrarMovimiento | El movimiento guarda el timer correcto")
  public void registrarMovimientoDeberiaAsociarElTimerCorrecto() {
    Timer timer = new Timer();

    servicioControlStock.registrarMovimiento(
      productoConStock(10),
      timer,
      2,
      TipoMovimientoStock.EGRESO
    );

    ArgumentCaptor<ControlStock> captor = ArgumentCaptor.forClass(ControlStock.class);
    verify(repositorioControlStockMock).guardar(captor.capture());
    assertEquals(timer, captor.getValue().getTimer());
  }

  @Test
  @DisplayName("HAP-04 | registrarMovimiento | El movimiento guarda la cantidad correcta")
  public void registrarMovimientoDeberiaGuardarLaCantidad() {
    servicioControlStock.registrarMovimiento(
      productoConStock(10),
      new Timer(),
      4,
      TipoMovimientoStock.EGRESO
    );

    ArgumentCaptor<ControlStock> captor = ArgumentCaptor.forClass(ControlStock.class);
    verify(repositorioControlStockMock).guardar(captor.capture());
    assertEquals(4, captor.getValue().getCantidad());
  }

  @Test
  @DisplayName("HAP-05 | registrarMovimiento | La fecha del movimiento es la hora actual del clock")
  public void registrarMovimientoDeberiaUsarFechaDelClock() {
    OffsetDateTime fechaEsperada = OffsetDateTime.now(clock);

    servicioControlStock.registrarMovimiento(
      productoConStock(10),
      new Timer(),
      1,
      TipoMovimientoStock.EGRESO
    );

    ArgumentCaptor<ControlStock> captor = ArgumentCaptor.forClass(ControlStock.class);
    verify(repositorioControlStockMock).guardar(captor.capture());
    assertEquals(fechaEsperada, captor.getValue().getFecha());
  }

  @Test
  @DisplayName("HAP-06 | registrarMovimiento | Tipo INGRESO se guarda correctamente")
  public void registrarMovimientoIngresoDeberiaGuardarTipoIngreso() {
    servicioControlStock.registrarMovimiento(
      productoConStock(5),
      null,
      10,
      TipoMovimientoStock.INGRESO
    );

    ArgumentCaptor<ControlStock> captor = ArgumentCaptor.forClass(ControlStock.class);
    verify(repositorioControlStockMock).guardar(captor.capture());
    assertEquals(TipoMovimientoStock.INGRESO, captor.getValue().getTipo());
  }

  @Test
  @DisplayName("HAP-07 | registrarMovimiento | Tipo EGRESO se guarda correctamente")
  public void registrarMovimientoEgresoDeberiaGuardarTipoEgreso() {
    servicioControlStock.registrarMovimiento(
      productoConStock(5),
      new Timer(),
      2,
      TipoMovimientoStock.EGRESO
    );

    ArgumentCaptor<ControlStock> captor = ArgumentCaptor.forClass(ControlStock.class);
    verify(repositorioControlStockMock).guardar(captor.capture());
    assertEquals(TipoMovimientoStock.EGRESO, captor.getValue().getTipo());
  }

  @Test
  @DisplayName("HAP-08 | registrarMovimiento | Timer null permitido para ajustes manuales")
  public void registrarMovimientoConTimerNuloDeberiaGuardar() {
    servicioControlStock.registrarMovimiento(
      productoConStock(5),
      null,
      2,
      TipoMovimientoStock.INGRESO
    );

    ArgumentCaptor<ControlStock> captor = ArgumentCaptor.forClass(ControlStock.class);
    verify(repositorioControlStockMock).guardar(captor.capture());
    assertNull(captor.getValue().getTimer());
  }

  private Producto productoConStock(int cantidad) {
    Producto producto = new Producto();
    producto.setCantidad(cantidad);
    return producto;
  }
}
