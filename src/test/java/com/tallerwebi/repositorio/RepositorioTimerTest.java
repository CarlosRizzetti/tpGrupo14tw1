package com.tallerwebi.repositorio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.*;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
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
  private Usuario usuarioTest;

  @BeforeEach
  public void init() {
    repositorioTimer = new RepositorioTimerImpl(sessionFactory);
    usuarioTest = new Usuario();
    sessionFactory.getCurrentSession().save(usuarioTest);
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
    Integer cantidad,
    Usuario usuario
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
      cantidad,
      usuario
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

    Timer timer = buildTimer("group-1", categoria, producto, regla, 1, usuarioTest);
    Timer timer2 = buildTimer("group-2", categoria, producto, regla, 1, usuarioTest);
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

    Timer timer = buildTimer("group-1", categoria, producto, regla, 1, usuarioTest);
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
    Timer timer = buildTimer("group-1", categoria, producto, regla, 1, new Usuario());

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
    Timer timer = buildTimer("group-1", categoria, producto, regla, 1, new Usuario());

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
    buildTimer("group-uuid-test", categoria, producto, regla, 1, usuarioTest);

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
    Timer timer = buildTimer("group-uuid-test", categoria, producto, regla, 1, usuarioTest);
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
    buildTimer("group-correcto", categoria, producto, regla, 1, usuarioTest);

    boolean resultado = repositorioTimer.existeTimerActivoEnCategoriaYGrupo(
      categoria.getId(),
      "group-incorrecto"
    );

    assertFalse(resultado);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-02 | obtenerTimersConFiltro | Sin filtros retorna todos los timers ordenados")
  public void obtenerTimersConFiltro_sinFiltros_deberiaRetornarTodos() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    Timer timer1 = buildTimer("group-1", categoria, producto, regla, 1, usuarioTest);
    timer1.setEstado(EstadoTimer.ACTIVO);
    sessionFactory.getCurrentSession().save(timer1);

    Timer timer2 = buildTimer("group-2", categoria, producto, regla, 1, usuarioTest);
    timer2.setEstado(EstadoTimer.ELIMINADO);
    sessionFactory.getCurrentSession().save(timer2);

    List<Timer> timers = repositorioTimer.obtenerTimersConFiltro(null, null);

    // Debería traer ambos ya que no hay filtros aplicados
    assertEquals(2, timers.size());
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-03 | obtenerTimersConFiltro | Con estado retorna solo los timers de ese estado")
  public void obtenerTimersConFiltro_conEstado_deberiaRetornarFiltrados() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    Timer timerActivo = buildTimer("group-1", categoria, producto, regla, 1, usuarioTest);
    timerActivo.setEstado(EstadoTimer.ACTIVO);
    sessionFactory.getCurrentSession().save(timerActivo);

    Timer timerEliminado = buildTimer("group-2", categoria, producto, regla, 1, usuarioTest);
    timerEliminado.setEstado(EstadoTimer.ELIMINADO);
    sessionFactory.getCurrentSession().save(timerEliminado);

    List<Timer> timers = repositorioTimer.obtenerTimersConFiltro(EstadoTimer.ACTIVO, null);

    assertEquals(1, timers.size());
    assertThat(EstadoTimer.ACTIVO, is(timers.get(0).getEstado()));
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "HP-04 | obtenerTimersConFiltro | Con categoria retorna solo los timers de esa categoria"
  )
  public void obtenerTimersConFiltro_conCategoria_deberiaRetornarFiltrados() {
    Categoria categoriaPrincipal = buildCategoria(); // Asumimos que el builder le asigna un nombre y la persiste

    Categoria categoriaSecundaria = buildCategoria();
    categoriaSecundaria.setNombre("Otra Categoria");
    // Asegurate de que el builder de categoria o tu código guarde esta entidad si es necesario
    // sessionFactory.getCurrentSession().save(categoriaSecundaria);

    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    Timer timerCat1 = buildTimer("group-1", categoriaPrincipal, producto, regla, 1, usuarioTest);
    sessionFactory.getCurrentSession().save(timerCat1);

    Timer timerCat2 = buildTimer("group-2", categoriaSecundaria, producto, regla, 1, usuarioTest);
    sessionFactory.getCurrentSession().save(timerCat2);

    List<Timer> timers = repositorioTimer.obtenerTimersConFiltro(null, categoriaPrincipal.getId());

    assertEquals(1, timers.size());
    assertThat(categoriaPrincipal.getNombre(), is(timers.get(0).getCategoria().getNombre()));
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-05 | obtenerTimersConFiltro | Con ambos filtros retorna la coincidencia exacta")
  public void obtenerTimersConFiltro_conAmbosFiltros_deberiaRetornarCoincidenciaExacta() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    // Match exacto: Activo y Categoria correcta
    Timer timerEsperado = buildTimer("group-1", categoria, producto, regla, 1, usuarioTest);
    timerEsperado.setEstado(EstadoTimer.ACTIVO);
    sessionFactory.getCurrentSession().save(timerEsperado);

    // Falla por estado
    Timer timerEliminado = buildTimer("group-2", categoria, producto, regla, 1, usuarioTest);
    timerEliminado.setEstado(EstadoTimer.ELIMINADO);
    sessionFactory.getCurrentSession().save(timerEliminado);

    // Ejecutar búsqueda con ambos parámetros
    List<Timer> timers = repositorioTimer.obtenerTimersConFiltro(
      EstadoTimer.ACTIVO,
      categoria.getId()
    );

    assertEquals(1, timers.size());
    assertThat(EstadoTimer.ACTIVO, is(timers.get(0).getEstado()));
    assertThat(categoria.getNombre(), is(timers.get(0).getCategoria().getNombre()));
    assertThat("group-1", is(timers.get(0).getGroupId()));
  }

  // ===================== obtenerTimersActivosConStockPorProducto =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "HP-06 | obtenerTimersActivosConStockPorProducto | Retorna timers activos con stock > 0"
  )
  public void obtenerTimersActivosConStockPorProducto_deberiaRetornarSoloActivosConStock() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    Timer t1 = buildTimer("g1", categoria, producto, regla, 5, usuarioTest);
    t1.setEstado(EstadoTimer.ACTIVO);
    sessionFactory.getCurrentSession().save(t1);

    Timer tSinStock = buildTimer("g2", categoria, producto, regla, 0, usuarioTest);
    tSinStock.setEstado(EstadoTimer.ACTIVO);
    sessionFactory.getCurrentSession().save(tSinStock);

    List<Timer> resultado = repositorioTimer.obtenerTimersActivosConStockPorProducto(
      producto.getId()
    );
    assertEquals(1, resultado.size());
    assertEquals("g1", resultado.get(0).getGroupId());
  }

  // ===================== obtenerTodosLosTimers =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "HP-07 | obtenerTodosLosTimers | Retorna todos los timers ordenados por fechaCreacion"
  )
  public void obtenerTodosLosTimers_deberiaRetornarTodos() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    buildTimer("g1", categoria, producto, regla, 1, usuarioTest);
    buildTimer("g2", categoria, producto, regla, 1, usuarioTest);

    List<Timer> resultado = repositorioTimer.obtenerTodosLosTimers();
    assertTrue(resultado.size() >= 2);
  }

  // ===================== obtenerFechasCreacionDesde =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-08 | obtenerFechasCreacionDesde | Retorna fechas desde el límite indicado")
  public void obtenerFechasCreacionDesde_deberiaRetornarFechas() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    Timer t = buildTimer("g1", categoria, producto, regla, 1, usuarioTest);
    t.getCicloVida().setFechaCreacion(OffsetDateTime.now());
    sessionFactory.getCurrentSession().save(t);

    List<OffsetDateTime> fechas = repositorioTimer.obtenerFechasCreacionDesde(
      OffsetDateTime.now().minusDays(1)
    );
    assertFalse(fechas.isEmpty());
  }

  // ===================== contarVencimientosPorProducto =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName(
    "HP-09 | contarVencimientosPorProducto | Retorna conteo agrupado por nombre de producto"
  )
  public void contarVencimientosPorProducto_deberiaRetornarAgrupación() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    producto.setNombre("Hamburguesa");
    sessionFactory.getCurrentSession().save(producto);
    ReglaVencimiento regla = buildRegla();

    Timer t = buildTimer("g1", categoria, producto, regla, 1, usuarioTest);
    t.getCicloVida().setFechaCreacion(OffsetDateTime.now());
    sessionFactory.getCurrentSession().save(t);

    List<Object[]> conteos = repositorioTimer.contarVencimientosPorProducto(
      OffsetDateTime.now().minusDays(1)
    );
    assertFalse(conteos.isEmpty());
    assertEquals("Hamburguesa", conteos.get(0)[0]);
  }

  // ===================== contarPorEstado =====================

  @Test
  @Transactional
  @Rollback
  @DisplayName("HP-10 | contarPorEstado | Retorna conteo agrupado por estado")
  public void contarPorEstado_deberiaRetornarConteoPorEstado() {
    Categoria categoria = buildCategoria();
    Producto producto = buildProducto();
    ReglaVencimiento regla = buildRegla();

    Timer t = buildTimer("g1", categoria, producto, regla, 1, usuarioTest);
    t.setEstado(EstadoTimer.ACTIVO);
    t.getCicloVida().setFechaCreacion(OffsetDateTime.now());
    sessionFactory.getCurrentSession().save(t);

    List<Object[]> conteos = repositorioTimer.contarPorEstado(OffsetDateTime.now().minusDays(1));
    assertFalse(conteos.isEmpty());
  }
}
