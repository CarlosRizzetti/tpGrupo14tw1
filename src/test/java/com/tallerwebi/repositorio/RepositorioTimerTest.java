package com.tallerwebi.repositorio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.time.OffsetDateTime;
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
public class RepositorioTimerTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTimer repositorioTimer;

  @BeforeEach
  public void init() {
    repositorioTimer = new RepositorioTimerImpl(sessionFactory);
  }

  // ===================== helpers =====================

  private Categoria buildCategoria() {
    Categoria categoria = new Categoria("mccafe.png", true, "mccafe");
    sessionFactory.getCurrentSession().save(categoria);
    return categoria;
  }

  private Producto buildProducto() {
    Producto producto = new Producto();
    sessionFactory.getCurrentSession().save(producto);
    return producto;
  }

  private ReglaVencimiento buildRegla() {
    ReglaVencimiento regla = new ReglaVencimiento();
    sessionFactory.getCurrentSession().save(regla);
    return regla;
  }

  private Timer buildTimer(
    String groupId,
    Categoria categoria,
    Producto producto,
    ReglaVencimiento regla
  ) {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusHours(2);
    Timer timer = new Timer(fechaCreacion, fechaVencimiento, groupId, producto, categoria, regla);
    sessionFactory.getCurrentSession().save(timer);
    return timer;
  }

  // ===================== obtenerTimersSegunEstado =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-01 | obtenerTimersSegunEstado | Retorna timers activos de una categoria")
  public void obtenerTodosLosTimersActivos() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    Timer timer = buildTimer("group-1", categoria, producto, regla);
    Timer timer2 = buildTimer("group-2", categoria, producto, regla);
    timer2.setEstado("inactivo");
    sessionFactory.getCurrentSession().save(timer2);

    List<Timer> timers = repositorioTimer.obtenerTimersSegunEstado(categoria.getId(), "activo");

    assertEquals(1, timers.size());
    assertThat(categoria.getNombre(), is(timers.get(0).getCategoria().getNombre()));
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "NEG-01 | obtenerTimersSegunEstado | Retorna lista vacía cuando no hay timers activos"
  )
  public void obtenerTimersSegunEstado_deberiaRetornarListaVaciaCuandoNoHayActivos() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    Timer timer = buildTimer("group-1", categoria, producto, regla);
    timer.setEstado("inactivo");
    sessionFactory.getCurrentSession().save(timer);

    List<Timer> timers = repositorioTimer.obtenerTimersSegunEstado(categoria.getId(), "activo");

    assertTrue(timers.isEmpty());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "NEG-02 | obtenerTimersSegunEstado | Retorna lista vacía cuando la categoria no existe"
  )
  public void obtenerTimersSegunEstado_deberiaRetornarListaVaciaCuandoCategoriaNoExiste() {
    List<Timer> timers = repositorioTimer.obtenerTimersSegunEstado(999L, "activo");

    assertTrue(timers.isEmpty());
  }

  // ===================== buscarPorId =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-02 | buscarPorId | Retorna el timer cuando existe")
  public void buscarPorId_deberiaRetornarElTimerCuandoExiste() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();
    Timer timer = buildTimer("group-1", categoria, producto, regla);

    Timer resultado = repositorioTimer.buscarPorId(timer.getId());

    assertNotNull(resultado);
    assertEquals(timer.getId(), resultado.getId());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("NEG-03 | buscarPorId | Retorna null cuando no existe")
  public void buscarPorId_deberiaRetornarNullCuandoNoExiste() {
    Timer resultado = repositorioTimer.buscarPorId(999L);

    assertNull(resultado);
  }

  // ===================== guardar =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-03 | guardar | Guarda un timer correctamente")
  public void guardar_deberiaGuardarUnTimerCorrectamente() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();
    Timer timer = buildTimer("group-1", categoria, producto, regla);

    Timer resultado = repositorioTimer.buscarPorId(timer.getId());

    assertNotNull(resultado);
    assertEquals("group-1", resultado.getGroupId());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-04 | guardar | El timer guardado tiene estaActivo en true por defecto")
  public void guardar_deberiaGuardarTimerConEstaActivoTrue() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();
    Timer timer = buildTimer("group-1", categoria, producto, regla);

    Timer resultado = repositorioTimer.buscarPorId(timer.getId());

    assertTrue(resultado.getEstaActivo());
  }

  // ===================== existeTimerActivoEnCategoriaYGrupo =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "HP-05 | existeTimerActivoEnCategoriaYGrupo | Retorna true cuando existe timer activo"
  )
  public void existeTimerActivo_deberiaRetornarTrueCuandoExiste() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();
    buildTimer("group-uuid-test", categoria, producto, regla);

    boolean resultado = repositorioTimer.existeTimerActivoEnCategoriaYGrupo(
      categoria.getId(),
      "group-uuid-test"
    );

    assertTrue(resultado);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "NEG-04 | existeTimerActivoEnCategoriaYGrupo | Retorna false cuando no existe timer activo"
  )
  public void existeTimerActivo_deberiaRetornarFalseCuandoNoExiste() {
    Categoria categoria = buildCategoria();

    boolean resultado = repositorioTimer.existeTimerActivoEnCategoriaYGrupo(
      categoria.getId(),
      "group-inexistente"
    );

    assertFalse(resultado);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "NEG-05 | existeTimerActivoEnCategoriaYGrupo | Retorna false cuando el timer existe pero está inactivo"
  )
  public void existeTimerActivo_deberiaRetornarFalseCuandoTimerEstaInactivo() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();
    Timer timer = buildTimer("group-uuid-test", categoria, producto, regla);
    timer.setEstaActivo(false);
    sessionFactory.getCurrentSession().save(timer);

    boolean resultado = repositorioTimer.existeTimerActivoEnCategoriaYGrupo(
      categoria.getId(),
      "group-uuid-test"
    );

    assertFalse(resultado);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "NEG-06 | existeTimerActivoEnCategoriaYGrupo | Retorna false cuando groupId no coincide"
  )
  public void existeTimerActivo_deberiaRetornarFalseCuandoGroupIdNoCoinicide() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();
    buildTimer("group-correcto", categoria, producto, regla);

    boolean resultado = repositorioTimer.existeTimerActivoEnCategoriaYGrupo(
      categoria.getId(),
      "group-incorrecto"
    );

    assertFalse(resultado);
  }
}
