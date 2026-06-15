package com.tallerwebi.repositorio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
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
    ReglaVencimiento regla,
    Integer cantidad
  ) {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusHours(2);
    Timer timer = new Timer(
      fechaCreacion,
      fechaVencimiento,
      groupId,
      producto,
      categoria,
      regla,
      cantidad
    );
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

    Timer timer = buildTimer("group-1", categoria, producto, regla, 1);
    Timer timer2 = buildTimer("group-2", categoria, producto, regla, 1);
    timer2.setEstado(EstadoTimer.ELIMINADO);
    sessionFactory.getCurrentSession().save(timer2);

    List<Timer> timers = repositorioTimer.obtenerTimersSegunEstado(
      categoria.getId(),
      EstadoTimer.ACTIVO
    );

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

    Timer timer = buildTimer("group-1", categoria, producto, regla, 1);
    timer.setEstado(EstadoTimer.ELIMINADO);
    sessionFactory.getCurrentSession().save(timer);

    List<Timer> timers = repositorioTimer.obtenerTimersSegunEstado(
      categoria.getId(),
      EstadoTimer.ACTIVO
    );

    assertTrue(timers.isEmpty());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "NEG-02 | obtenerTimersSegunEstado | Retorna lista vacía cuando la categoria no existe"
  )
  public void obtenerTimersSegunEstado_deberiaRetornarListaVaciaCuandoCategoriaNoExiste() {
    List<Timer> timers = repositorioTimer.obtenerTimersSegunEstado(999L, EstadoTimer.ACTIVO);

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
    Timer timer = buildTimer("group-1", categoria, producto, regla, 1);

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
    Timer timer = buildTimer("group-1", categoria, producto, regla, 1);

    Timer resultado = repositorioTimer.buscarPorId(timer.getId());

    assertNotNull(resultado);
    assertEquals("group-1", resultado.getGroupId());
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
    buildTimer("group-uuid-test", categoria, producto, regla, 1);

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
    Timer timer = buildTimer("group-uuid-test", categoria, producto, regla, 1);
    timer.setEstado(EstadoTimer.ELIMINADO);
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
    buildTimer("group-correcto", categoria, producto, regla, 1);

    boolean resultado = repositorioTimer.existeTimerActivoEnCategoriaYGrupo(
      categoria.getId(),
      "group-incorrecto"
    );

    assertFalse(resultado);
  }

  // ===================== helpers estadísticas =====================

  private Producto buildProducto(String nombre) {
    Producto producto = new Producto();
    producto.setNombre(nombre);
    sessionFactory.getCurrentSession().save(producto);
    return producto;
  }

  private OffsetDateTime fecha(int anio, int mes, int dia, int hora) {
    return OffsetDateTime.of(anio, mes, dia, hora, 0, 0, 0, ZoneOffset.ofHours(-3));
  }

  private Timer buildTimerCreado(
    OffsetDateTime fechaCreacion,
    EstadoTimer estado,
    Producto producto
  ) {
    Timer timer = new Timer(
      fechaCreacion,
      fechaCreacion.plusHours(2),
      "grp-est",
      producto,
      null,
      null,
      1
    );
    timer.setEstado(estado);
    sessionFactory.getCurrentSession().save(timer);
    return timer;
  }

  private long contar(List<Object[]> filas, EstadoTimer estado) {
    return filas
      .stream()
      .filter(fila -> fila[0] == estado)
      .mapToLong(fila -> ((Number) fila[1]).longValue())
      .findFirst()
      .orElse(0L);
  }

  // ===================== obtenerFechasCreacionDesde =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-06 | obtenerFechasCreacionDesde | Filtra por la fecha de inicio")
  public void obtenerFechasCreacionDesde_deberiaFiltrarPorFecha() {
    Producto producto = buildProducto("Hamburguesa");
    buildTimerCreado(fecha(2026, 6, 10, 9), EstadoTimer.ACTIVO, producto);
    buildTimerCreado(fecha(2026, 6, 1, 9), EstadoTimer.ACTIVO, producto);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioTimer.obtenerFechasCreacionDesde(
      fecha(2026, 6, 5, 0)
    );

    assertEquals(1, resultado.size());
  }

  // ===================== contarVencimientosPorProducto =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-07 | contarVencimientosPorProducto | Agrupa y ordena por cantidad descendente")
  public void contarVencimientosPorProducto_deberiaAgruparYOrdenar() {
    Producto hamburguesa = buildProducto("Hamburguesa");
    Producto cafe = buildProducto("Café");
    buildTimerCreado(fecha(2026, 6, 10, 9), EstadoTimer.ACTIVO, hamburguesa);
    buildTimerCreado(fecha(2026, 6, 11, 9), EstadoTimer.ACTIVO, hamburguesa);
    buildTimerCreado(fecha(2026, 6, 10, 9), EstadoTimer.ACTIVO, cafe);
    sessionFactory.getCurrentSession().flush();

    List<Object[]> resultado = repositorioTimer.contarVencimientosPorProducto(fecha(2026, 6, 5, 0));

    assertEquals(2, resultado.size());
    assertEquals("Hamburguesa", resultado.get(0)[0]);
    assertEquals(2L, ((Number) resultado.get(0)[1]).longValue());
  }

  // ===================== contarPorEstado =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-08 | contarPorEstado | Agrupa los vencimientos por estado")
  public void contarPorEstado_deberiaAgruparPorEstado() {
    Producto producto = buildProducto("Hamburguesa");
    buildTimerCreado(fecha(2026, 6, 10, 9), EstadoTimer.VENCIDO, producto);
    buildTimerCreado(fecha(2026, 6, 11, 9), EstadoTimer.VENCIDO, producto);
    buildTimerCreado(fecha(2026, 6, 12, 9), EstadoTimer.RENOVADO, producto);
    sessionFactory.getCurrentSession().flush();

    List<Object[]> resultado = repositorioTimer.contarPorEstado(fecha(2026, 6, 5, 0));

    assertEquals(2L, contar(resultado, EstadoTimer.VENCIDO));
    assertEquals(1L, contar(resultado, EstadoTimer.RENOVADO));
    assertEquals(0L, contar(resultado, EstadoTimer.IMPORTADO));
  }
}
