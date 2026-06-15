package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.ControlStock;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.RepositorioEstadistica;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import javax.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@ActiveProfiles("test")
public class RepositorioEstadisticaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioEstadistica repositorioEstadistica;

  @BeforeEach
  public void init() {
    repositorioEstadistica = new RepositorioEstadisticaImpl(sessionFactory);
  }

  private OffsetDateTime fecha(int anio, int mes, int dia, int hora) {
    return OffsetDateTime.of(anio, mes, dia, hora, 0, 0, 0, ZoneOffset.ofHours(-3));
  }

  private Producto persistirProducto() {
    return persistirProducto("Producto Test");
  }

  private Producto persistirProducto(String nombre) {
    Producto producto = new Producto();
    producto.setNombre(nombre);
    producto.setCantidad(10);
    sessionFactory.getCurrentSession().save(producto);
    return producto;
  }

  private void persistirTimer(OffsetDateTime fechaCreacion) {
    persistirTimer(fechaCreacion, null);
  }

  private void persistirTimer(OffsetDateTime fechaCreacion, Producto producto) {
    Timer timer = Timer
      .builder()
      .fechaCreacion(fechaCreacion)
      .fechaVencimiento(fechaCreacion.plusHours(2))
      .cantidadProducto(1)
      .estado(EstadoTimer.ACTIVO)
      .producto(producto)
      .build();
    sessionFactory.getCurrentSession().save(timer);
  }

  private void persistirMovimiento(
    Producto producto,
    OffsetDateTime fecha,
    TipoMovimientoStock tipo
  ) {
    ControlStock movimiento = ControlStock
      .builder()
      .producto(producto)
      .cantidad(1)
      .tipo(tipo)
      .fecha(fecha)
      .build();
    sessionFactory.getCurrentSession().save(movimiento);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "HP-01 | obtenerFechasCreacionVencimientos | Devuelve solo las fechas desde el límite"
  )
  public void obtenerFechasCreacionVencimientosDeberiaFiltrarPorFechaDesde() {
    persistirTimer(fecha(2026, 6, 10, 9));
    persistirTimer(fecha(2026, 6, 1, 9));
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioEstadistica.obtenerFechasCreacionVencimientos(
      fecha(2026, 6, 5, 0)
    );

    assertEquals(1, resultado.size());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-02 | obtenerFechasModificacionesStock | Devuelve ingresos y egresos del rango")
  public void obtenerFechasModificacionesStockDeberiaIncluirTodosLosTipos() {
    Producto producto = persistirProducto();
    persistirMovimiento(producto, fecha(2026, 6, 10, 9), TipoMovimientoStock.INGRESO);
    persistirMovimiento(producto, fecha(2026, 6, 10, 12), TipoMovimientoStock.EGRESO);
    persistirMovimiento(producto, fecha(2026, 6, 1, 9), TipoMovimientoStock.EGRESO);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioEstadistica.obtenerFechasModificacionesStock(
      fecha(2026, 6, 5, 0)
    );

    assertEquals(2, resultado.size());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-03 | obtenerFechasDemanda | Devuelve únicamente los movimientos de egreso")
  public void obtenerFechasDemandaDeberiaDevolverSoloEgresos() {
    Producto producto = persistirProducto();
    persistirMovimiento(producto, fecha(2026, 6, 10, 9), TipoMovimientoStock.INGRESO);
    persistirMovimiento(producto, fecha(2026, 6, 10, 12), TipoMovimientoStock.EGRESO);
    persistirMovimiento(producto, fecha(2026, 6, 11, 12), TipoMovimientoStock.EGRESO);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioEstadistica.obtenerFechasDemanda(
      fecha(2026, 6, 5, 0)
    );

    assertEquals(2, resultado.size());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("NEG-01 | obtenerFechasDemanda | Devuelve lista vacía cuando no hay egresos")
  public void obtenerFechasDemandaDeberiaDevolverListaVaciaSinEgresos() {
    Producto producto = persistirProducto();
    persistirMovimiento(producto, fecha(2026, 6, 10, 9), TipoMovimientoStock.INGRESO);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioEstadistica.obtenerFechasDemanda(
      fecha(2026, 6, 5, 0)
    );

    assertTrue(resultado.isEmpty());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "HP-04 | obtenerConteoVencimientosPorProducto | Agrupa y ordena por cantidad descendente"
  )
  public void obtenerConteoVencimientosPorProductoDeberiaAgruparYOrdenar() {
    Producto hamburguesa = persistirProducto("Hamburguesa");
    Producto cafe = persistirProducto("Café");
    persistirTimer(fecha(2026, 6, 10, 9), hamburguesa);
    persistirTimer(fecha(2026, 6, 11, 9), hamburguesa);
    persistirTimer(fecha(2026, 6, 10, 9), cafe);
    sessionFactory.getCurrentSession().flush();

    List<Object[]> resultado = repositorioEstadistica.obtenerConteoVencimientosPorProducto(
      fecha(2026, 6, 5, 0)
    );

    assertEquals(2, resultado.size());
    assertEquals("Hamburguesa", resultado.get(0)[0]);
    assertEquals(2L, ((Number) resultado.get(0)[1]).longValue());
    assertEquals("Café", resultado.get(1)[0]);
    assertEquals(1L, ((Number) resultado.get(1)[1]).longValue());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "NEG-02 | obtenerConteoVencimientosPorProducto | Ignora timers sin producto y fuera de rango"
  )
  public void obtenerConteoVencimientosPorProductoDeberiaIgnorarSinProductoYFueraDeRango() {
    Producto hamburguesa = persistirProducto("Hamburguesa");
    persistirTimer(fecha(2026, 6, 10, 9), hamburguesa);
    persistirTimer(fecha(2026, 1, 1, 9), hamburguesa);
    persistirTimer(fecha(2026, 6, 10, 9), null);
    sessionFactory.getCurrentSession().flush();

    List<Object[]> resultado = repositorioEstadistica.obtenerConteoVencimientosPorProducto(
      fecha(2026, 6, 5, 0)
    );

    assertEquals(1, resultado.size());
    assertEquals(1L, ((Number) resultado.get(0)[1]).longValue());
  }
}
