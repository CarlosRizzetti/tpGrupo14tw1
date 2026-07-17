package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.enums.EstadoLote;
import com.tallerwebi.dominio.interfaces.RepositorioLote;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
public class RepositorioLoteTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioLote repositorioLote;
  private Clock clock;
  private RepositorioProducto repositorioProducto;

  @BeforeEach
  public void init() {
    repositorioLote = new RepositorioLoteImpl(sessionFactory);
    clock = Clock.fixed(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.ofHours(-3));
    repositorioProducto = new RepositorioProductoImpl(sessionFactory);
  }

  // ---------- helpers ----------

  private Producto crearYGuardarProducto(String nombre) {
    Producto producto = new Producto();
    producto.setNombre(nombre);
    producto.setEstaActivo(true);
    repositorioProducto.guardar(producto);
    return producto;
  }

  private Lote crearLote(
    Producto producto,
    Integer cantidadDisponible,
    EstadoLote estado,
    OffsetDateTime fechaVencimiento
  ) {
    Lote lote = new Lote();
    lote.setProducto(producto);
    lote.setCantidadInicial(cantidadDisponible);
    lote.setCantidadDisponible(cantidadDisponible);
    lote.setEstado(estado);
    lote.setFechaDeIngreso(OffsetDateTime.now().minusDays(30));
    lote.setFechaDeVencimiento(fechaVencimiento);
    lote.setProveedor("Proveedor Test");
    lote.setMarca("Marca Test");
    lote.setNumeroDeLote(1L);
    return lote;
  }

  // ========================================================
  // guardar
  // ========================================================

  @Test
  @DisplayName("HP-01 | guardar | Persiste el lote y le asigna un id")
  public void guardarLoteDeberiaPersistirloYAsignarleId() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote lote = crearLote(producto, 20, EstadoLote.DISPONIBLE, OffsetDateTime.now().plusDays(10));

    repositorioLote.guardar(lote);
    sessionFactory.getCurrentSession().flush();

    assertTrue(lote.getId() != null);
  }

  // ========================================================
  // buscarPorId
  // ========================================================

  @Test
  @DisplayName("HP-01 | buscarPorId | Devuelve el lote previamente guardado")
  public void buscarPorIdDeberiaDevolverElLoteGuardado() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote lote = crearLote(producto, 20, EstadoLote.DISPONIBLE, OffsetDateTime.now().plusDays(10));
    repositorioLote.guardar(lote);
    sessionFactory.getCurrentSession().flush();

    Lote resultado = repositorioLote.buscarPorId(lote.getId());

    assertEquals(lote.getId(), resultado.getId());
    assertEquals(20, resultado.getCantidadDisponible());
  }

  @Test
  @DisplayName("EDGE-01 | buscarPorId | Devuelve null si el id no existe")
  public void buscarPorIdInexistenteDeberiaDevolverNull() {
    Lote resultado = repositorioLote.buscarPorId(999999L);

    assertNull(resultado);
  }

  // ========================================================
  // actualizar
  // ========================================================

  @Test
  @DisplayName("HP-01 | actualizar | Persiste los cambios realizados sobre un lote existente")
  public void actualizarLoteDeberiaPersistirLosCambios() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote lote = crearLote(producto, 20, EstadoLote.DISPONIBLE, OffsetDateTime.now().plusDays(10));
    repositorioLote.guardar(lote);
    sessionFactory.getCurrentSession().flush();

    lote.setCantidadDisponible(5);
    lote.setEstado(EstadoLote.EN_USO);
    repositorioLote.actualizar(lote);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Lote resultado = repositorioLote.buscarPorId(lote.getId());

    assertEquals(5, resultado.getCantidadDisponible());
    assertEquals(EstadoLote.EN_USO, resultado.getEstado());
  }

  // ========================================================
  // listarTodos
  // ========================================================

  @Test
  @DisplayName(
    "HP-01 | listarTodos | Ordena por nombre de producto asc y luego por fecha de vencimiento asc"
  )
  public void listarTodosDeberiaOrdenarPorNombreDeProductoYFechaDeVencimiento() {
    Producto queso = crearYGuardarProducto("Queso");
    Producto arroz = crearYGuardarProducto("Arroz");
    Lote quesoLejano = crearLote(
      queso,
      10,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(20)
    );
    Lote quesoCercano = crearLote(
      queso,
      10,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(5)
    );
    Lote arrozLote = crearLote(arroz, 10, EstadoLote.DISPONIBLE, OffsetDateTime.now().plusDays(15));
    repositorioLote.guardar(quesoLejano);
    repositorioLote.guardar(quesoCercano);
    repositorioLote.guardar(arrozLote);
    sessionFactory.getCurrentSession().flush();

    List<Lote> resultado = repositorioLote.listarTodos();

    assertEquals(3, resultado.size());
    assertEquals(arrozLote.getId(), resultado.get(0).getId());
    assertEquals(quesoCercano.getId(), resultado.get(1).getId());
    assertEquals(quesoLejano.getId(), resultado.get(2).getId());
  }

  @Test
  @DisplayName("EDGE-01 | listarTodos | Devuelve lista vacía si no hay lotes cargados")
  public void listarTodosSinLotesDeberiaDevolverListaVacia() {
    List<Lote> resultado = repositorioLote.listarTodos();

    assertTrue(resultado.isEmpty());
  }

  // ========================================================
  // listarConsumiblesDeProducto
  // ========================================================

  @Test
  @DisplayName(
    "HP-01 | listarConsumiblesDeProducto | Devuelve solo lotes DISPONIBLE/EN_USO con stock, ordenados por vencimiento"
  )
  public void listarConsumiblesDeProductoDeberiaFiltrarPorEstadoYCantidadYOrdenarPorVencimiento() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote enUsoLejano = crearLote(
      producto,
      10,
      EstadoLote.EN_USO,
      OffsetDateTime.now().plusDays(20)
    );
    Lote disponibleCercano = crearLote(
      producto,
      5,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(5)
    );
    Lote consumido = crearLote(producto, 0, EstadoLote.CONSUMIDO, OffsetDateTime.now().plusDays(1));
    Lote vencido = crearLote(producto, 8, EstadoLote.VENCIDO, OffsetDateTime.now().minusDays(2));
    Lote descartado = crearLote(
      producto,
      3,
      EstadoLote.DESCARTADO,
      OffsetDateTime.now().plusDays(30)
    );
    Lote sinStock = crearLote(producto, 0, EstadoLote.DISPONIBLE, OffsetDateTime.now().plusDays(3));
    repositorioLote.guardar(enUsoLejano);
    repositorioLote.guardar(disponibleCercano);
    repositorioLote.guardar(consumido);
    repositorioLote.guardar(vencido);
    repositorioLote.guardar(descartado);
    repositorioLote.guardar(sinStock);
    sessionFactory.getCurrentSession().flush();

    List<Lote> resultado = repositorioLote.listarConsumiblesDeProducto(producto.getId());

    assertEquals(2, resultado.size());
    assertEquals(disponibleCercano.getId(), resultado.get(0).getId());
    assertEquals(enUsoLejano.getId(), resultado.get(1).getId());
  }

  @Test
  @DisplayName(
    "EDGE-01 | listarConsumiblesDeProducto | Devuelve lista vacía si ningún lote está en estado consumible"
  )
  public void listarConsumiblesDeProductoSinLotesConsumiblesDeberiaDevolverListaVacia() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote consumido = crearLote(producto, 0, EstadoLote.CONSUMIDO, OffsetDateTime.now().plusDays(1));
    repositorioLote.guardar(consumido);
    sessionFactory.getCurrentSession().flush();

    List<Lote> resultado = repositorioLote.listarConsumiblesDeProducto(producto.getId());

    assertTrue(resultado.isEmpty());
  }

  @Test
  @DisplayName(
    "EDGE-02 | listarConsumiblesDeProducto | Devuelve lista vacía si el producto no tiene ningún lote"
  )
  public void listarConsumiblesDeProductoSinLotesDeberiaDevolverListaVacia() {
    Producto producto = crearYGuardarProducto("Queso");

    List<Lote> resultado = repositorioLote.listarConsumiblesDeProducto(producto.getId());

    assertTrue(resultado.isEmpty());
  }

  // ========================================================
  // buscarEnUsoDeProducto
  // ========================================================

  @Test
  @DisplayName("HP-01 | buscarEnUsoDeProducto | Devuelve el lote en estado EN_USO del producto")
  public void buscarEnUsoDeProductoDeberiaDevolverElLoteEnUso() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote enUso = crearLote(producto, 10, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(5));
    Lote disponible = crearLote(
      producto,
      10,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(15)
    );
    repositorioLote.guardar(enUso);
    repositorioLote.guardar(disponible);
    sessionFactory.getCurrentSession().flush();

    Lote resultado = repositorioLote.buscarEnUsoDeProducto(producto.getId());

    assertEquals(enUso.getId(), resultado.getId());
  }

  @Test
  @DisplayName(
    "EDGE-01 | buscarEnUsoDeProducto | Devuelve null si el producto no tiene ningún lote EN_USO"
  )
  public void buscarEnUsoDeProductoSinLoteEnUsoDeberiaDevolverNull() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote disponible = crearLote(
      producto,
      10,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(15)
    );
    repositorioLote.guardar(disponible);
    sessionFactory.getCurrentSession().flush();

    Lote resultado = repositorioLote.buscarEnUsoDeProducto(producto.getId());

    assertNull(resultado);
  }

  // ========================================================
  // obtenerFechasIngresoDesde
  // ========================================================

  @Test
  @DisplayName(
    "HP-01 | obtenerFechasIngresoDesde | Devuelve las fechas de ingreso posteriores o iguales al límite dado"
  )
  public void obtenerFechasIngresoDesdeDeberiaDevolverFechasDentroDelRango() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote dentroDelRango = crearLote(
      producto,
      10,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now(clock).plusDays(10)
    );
    dentroDelRango.setFechaDeIngreso(OffsetDateTime.now(clock).minusDays(2));
    Lote fueraDelRango = crearLote(
      producto,
      10,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now(clock).plusDays(10)
    );
    fueraDelRango.setFechaDeIngreso(OffsetDateTime.now(clock).minusDays(10));
    repositorioLote.guardar(dentroDelRango);
    repositorioLote.guardar(fueraDelRango);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioLote.obtenerFechasIngresoDesde(
      OffsetDateTime.now(clock).minusDays(5)
    );

    assertEquals(1, resultado.size());
    assertTrue(resultado.get(0).isEqual(dentroDelRango.getFechaDeIngreso()));
  }

  @Test
  @DisplayName(
    "EDGE-01 | obtenerFechasIngresoDesde | Devuelve lista vacía si ningún lote ingresó dentro del rango"
  )
  public void obtenerFechasIngresoDesdeSinCoincidenciasDeberiaDevolverListaVacia() {
    Producto producto = crearYGuardarProducto("Queso");
    Lote lote = crearLote(producto, 10, EstadoLote.DISPONIBLE, OffsetDateTime.now().plusDays(10));
    lote.setFechaDeIngreso(OffsetDateTime.now().minusDays(60));
    repositorioLote.guardar(lote);
    sessionFactory.getCurrentSession().flush();

    List<OffsetDateTime> resultado = repositorioLote.obtenerFechasIngresoDesde(
      OffsetDateTime.now().minusDays(5)
    );

    assertTrue(resultado.isEmpty());
  }
}
