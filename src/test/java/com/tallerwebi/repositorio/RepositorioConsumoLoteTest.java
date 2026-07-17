package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.entity.ConsumoLote;
import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoLote;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.RepositorioConsumoLote;
import com.tallerwebi.dominio.interfaces.RepositorioLote;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@ActiveProfiles("test")
@Transactional
public class RepositorioConsumoLoteTest {

  private static final Clock CLOCK = Clock.fixed(
    Instant.now().truncatedTo(ChronoUnit.SECONDS),
    ZoneOffset.ofHours(-3)
  );

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioConsumoLote repositorioConsumoLote;
  private RepositorioProducto repositorioProducto;
  private RepositorioLote repositorioLote;

  @BeforeEach
  public void init() {
    repositorioConsumoLote = new RepositorioConsumoLoteImpl(sessionFactory);
    repositorioProducto = new RepositorioProductoImpl(sessionFactory);
    repositorioLote = new RepositorioLoteImpl(sessionFactory);
  }

  // ---------- helpers ----------

  private Producto crearYGuardarProducto(String nombre) {
    Producto producto = new Producto();
    producto.setNombre(nombre);
    producto.setEstaActivo(true);
    repositorioProducto.guardar(producto);
    return producto;
  }

  private Lote crearYGuardarLote(Producto producto, Integer cantidadDisponible) {
    Lote lote = new Lote();
    lote.setProducto(producto);
    lote.setCantidadInicial(cantidadDisponible);
    lote.setCantidadDisponible(cantidadDisponible);
    lote.setEstado(EstadoLote.EN_USO);
    lote.setFechaDeIngreso(OffsetDateTime.now(CLOCK).minusDays(30));
    lote.setFechaDeVencimiento(OffsetDateTime.now(CLOCK).plusDays(10));
    lote.setProveedor("Proveedor Test");
    lote.setMarca("Marca Test");
    lote.setNumeroDeLote(1L);
    repositorioLote.guardar(lote);
    return lote;
  }

  private Timer crearYGuardarTimer() {
    Timer timer = Timer.builder().estado(EstadoTimer.VENCIDO).build();
    sessionFactory.getCurrentSession().save(timer);
    return timer;
  }

  private ConsumoLote crearConsumo(Lote lote, Timer timer, Integer cantidadConsumida) {
    ConsumoLote consumo = new ConsumoLote();
    consumo.setLote(lote);
    consumo.setTimer(timer);
    consumo.setCantidadConsumida(cantidadConsumida);
    return consumo;
  }

  // ========================================================
  // guardar
  // ========================================================

  @Test
  @DisplayName("HP-01 | guardar | Persiste el consumo y le asigna un id")
  public void guardarConsumoDeberiaPersistirloYAsignarleId() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote lote = crearYGuardarLote(producto, 20);
    Timer timer = crearYGuardarTimer();
    ConsumoLote consumo = crearConsumo(lote, timer, 5);

    repositorioConsumoLote.guardar(consumo);
    sessionFactory.getCurrentSession().flush();

    assertTrue(consumo.getId() != null);
  }

  @Test
  @DisplayName(
    "HP-02 | guardar | Persiste correctamente el lote, el timer y la cantidad consumida asociados"
  )
  public void guardarConsumoDeberiaPersistirLosDatosAsociadosCorrectamente() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote lote = crearYGuardarLote(producto, 20);
    Timer timer = crearYGuardarTimer();
    ConsumoLote consumo = crearConsumo(lote, timer, 8);

    repositorioConsumoLote.guardar(consumo);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    ConsumoLote resultado = sessionFactory
      .getCurrentSession()
      .get(ConsumoLote.class, consumo.getId());

    assertEquals(lote.getId(), resultado.getLote().getId());
    assertEquals(timer.getId(), resultado.getTimer().getId());
    assertEquals(8, resultado.getCantidadConsumida());
  }

  @Test
  @DisplayName("EDGE-01 | guardar | Persiste varios consumos del mismo lote sin pisarse entre sí")
  public void guardarVariosConsumosDelMismoLoteDeberianQuedarIndependientes() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote lote = crearYGuardarLote(producto, 20);
    Timer timerA = crearYGuardarTimer();
    Timer timerB = crearYGuardarTimer();
    ConsumoLote consumoA = crearConsumo(lote, timerA, 3);
    ConsumoLote consumoB = crearConsumo(lote, timerB, 7);

    repositorioConsumoLote.guardar(consumoA);
    repositorioConsumoLote.guardar(consumoB);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    ConsumoLote resultadoA = sessionFactory
      .getCurrentSession()
      .get(ConsumoLote.class, consumoA.getId());
    ConsumoLote resultadoB = sessionFactory
      .getCurrentSession()
      .get(ConsumoLote.class, consumoB.getId());

    assertNotEquals(resultadoA.getId(), resultadoB.getId());
    assertEquals(3, resultadoA.getCantidadConsumida());
    assertEquals(7, resultadoB.getCantidadConsumida());
  }
}
