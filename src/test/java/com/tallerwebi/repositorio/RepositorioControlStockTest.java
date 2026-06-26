package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.ControlStock;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import com.tallerwebi.dominio.interfaces.RepositorioControlStock;
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
public class RepositorioControlStockTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioControlStock repositorioControlStock;

  @BeforeEach
  public void init() {
    repositorioControlStock = new RepositorioControlStockImpl(sessionFactory);
  }

  private OffsetDateTime fecha(int anio, int mes, int dia, int hora) {
    return OffsetDateTime.of(anio, mes, dia, hora, 0, 0, 0, ZoneOffset.ofHours(-3));
  }

  private Producto persistirProducto() {
    Producto producto = new Producto();
    producto.setNombre("Producto Test");
    producto.setCantidad(10);
    sessionFactory.getCurrentSession().save(producto);
    return producto;
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
  @DisplayName("HP-01 | obtenerFechasMovimientosDesde | Devuelve ingresos y egresos del rango")
  public void obtenerFechasMovimientosDesde_deberiaIncluirTodosLosTipos() {
    Producto producto = persistirProducto();
    persistirMovimiento(producto, fecha(2026, 6, 10, 9), TipoMovimientoStock.INGRESO);
    persistirMovimiento(producto, fecha(2026, 6, 10, 12), TipoMovimientoStock.EGRESO);
    persistirMovimiento(producto, fecha(2026, 6, 1, 9), TipoMovimientoStock.EGRESO);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioControlStock.obtenerFechasMovimientosDesde(
      fecha(2026, 6, 5, 0)
    );

    assertEquals(2, resultado.size());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-02 | obtenerFechasEgresosDesde | Devuelve únicamente los egresos")
  public void obtenerFechasEgresosDesde_deberiaDevolverSoloEgresos() {
    Producto producto = persistirProducto();
    persistirMovimiento(producto, fecha(2026, 6, 10, 9), TipoMovimientoStock.INGRESO);
    persistirMovimiento(producto, fecha(2026, 6, 10, 12), TipoMovimientoStock.EGRESO);
    persistirMovimiento(producto, fecha(2026, 6, 11, 12), TipoMovimientoStock.EGRESO);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioControlStock.obtenerFechasEgresosDesde(
      fecha(2026, 6, 5, 0)
    );

    assertEquals(2, resultado.size());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("NEG-01 | obtenerFechasEgresosDesde | Lista vacía cuando no hay egresos")
  public void obtenerFechasEgresosDesde_deberiaDevolverListaVaciaSinEgresos() {
    Producto producto = persistirProducto();
    persistirMovimiento(producto, fecha(2026, 6, 10, 9), TipoMovimientoStock.INGRESO);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioControlStock.obtenerFechasEgresosDesde(
      fecha(2026, 6, 5, 0)
    );

    assertTrue(resultado.isEmpty());
  }
}
