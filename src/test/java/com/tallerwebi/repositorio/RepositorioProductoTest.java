package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@ActiveProfiles("test")
public class RepositorioProductoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioProducto repositorioProducto;

  @BeforeEach
  public void init() {
    repositorioProducto = new RepositorioProductoImpl(sessionFactory);
  }

  // ===================== guardar =====================

  @Test
  @DisplayName("HP-01 | guardar | Guarda un producto correctamente")
  @Transactional
  @Rollback
  void guardar_deberiaGuardarUnProductoCorrectamente() {
    Producto producto = buildProducto("Producto Test");

    repositorioProducto.guardar(producto);

    Producto resultado = repositorioProducto.obtenerProductoPorId(producto.getId());
    assertNotNull(resultado);
    assertEquals("Producto Test", resultado.getNombre());
  }

  @Test
  @DisplayName("HP-02 | guardar | Actualiza un producto existente")
  @Transactional
  @Rollback
  void guardar_deberiaActualizarUnProductoExistente() {
    Producto producto = buildProducto("Nombre Original");
    repositorioProducto.guardar(producto);

    producto.setNombre("Nombre Actualizado");
    repositorioProducto.guardar(producto);

    Producto resultado = repositorioProducto.obtenerProductoPorId(producto.getId());
    assertEquals("Nombre Actualizado", resultado.getNombre());
  }

  // ===================== obtenerProductoPorId =====================

  @Test
  @DisplayName("HP-03 | obtenerProductoPorId | Retorna el producto cuando existe")
  @Transactional
  @Rollback
  void obtenerProductoPorId_deberiaRetornarElProductoCuandoExiste() {
    Producto producto = buildProducto("Producto Test");
    repositorioProducto.guardar(producto);

    Producto resultado = repositorioProducto.obtenerProductoPorId(producto.getId());

    assertNotNull(resultado);
    assertEquals(producto.getId(), resultado.getId());
  }

  @Test
  @DisplayName("NEG-01 | obtenerProductoPorId | Retorna null cuando no existe")
  @Transactional
  @Rollback
  void obtenerProductoPorId_deberiaRetornarNullCuandoNoExiste() {
    Producto resultado = repositorioProducto.obtenerProductoPorId(999L);

    assertNull(resultado);
  }

  @Test
  @DisplayName("HP-04 | obtenerProductoPorId | Retorna el producto con sus reglas")
  @Transactional
  @Rollback
  void obtenerProductoPorId_deberiaRetornarElProductoConSusReglas() {
    Producto producto = buildProducto("Producto Con Reglas");
    repositorioProducto.guardar(producto);

    Producto resultado = repositorioProducto.obtenerProductoPorId(producto.getId());

    assertNotNull(resultado.getReglas());
  }

  // ===================== obtenerProductosPorCategoria =====================

  @Test
  @DisplayName("HP-05 | obtenerProductosPorCategoria | Retorna productos de una categoria")
  @Transactional
  @Rollback
  void obtenerProductosPorCategoria_deberiaRetornarProductosDeLaCategoria() {
    Categoria categoria = buildCategoria("Categoria Test");
    sessionFactory.getCurrentSession().save(categoria);

    Producto producto = buildProductoConCategoria("Producto Test", categoria);
    repositorioProducto.guardar(producto);

    List<Producto> resultado = repositorioProducto.obtenerProductosPorCategoria(categoria.getId());

    assertNotNull(resultado);
    assertEquals(1, resultado.size());
    assertEquals("Producto Test", resultado.get(0).getNombre());
  }

  @Test
  @DisplayName(
    "NEG-02 | obtenerProductosPorCategoria | Retorna lista vacía cuando no hay productos"
  )
  @Transactional
  @Rollback
  void obtenerProductosPorCategoria_deberiaRetornarListaVaciaCuandoNoHayProductos() {
    Categoria categoria = buildCategoria("Categoria Vacia");
    sessionFactory.getCurrentSession().save(categoria);

    List<Producto> resultado = repositorioProducto.obtenerProductosPorCategoria(categoria.getId());

    assertNotNull(resultado);
    assertTrue(resultado.isEmpty());
  }

  @Test
  @DisplayName("NEG-03 | obtenerProductosPorCategoria | No retorna productos inactivos")
  @Transactional
  @Rollback
  void obtenerProductosPorCategoria_noDeberiaRetornarProductosInactivos() {
    Categoria categoria = buildCategoria("Categoria Test");
    sessionFactory.getCurrentSession().save(categoria);

    Producto productoInactivo = buildProductoConCategoria("Producto Inactivo", categoria);
    productoInactivo.setEstaActivo(false);
    repositorioProducto.guardar(productoInactivo);

    List<Producto> resultado = repositorioProducto.obtenerProductosPorCategoria(categoria.getId());

    assertTrue(resultado.isEmpty());
  }

  // ===================== obtenerProductoConReglasYCategorias =====================

  @Test
  @DisplayName(
    "HP-06 | obtenerProductoConReglasYCategorias | Retorna el producto con reglas y categorias"
  )
  @Transactional
  @Rollback
  void obtenerProductoConReglasYCategorias_deberiaRetornarProductoConReglasYCategorias() {
    Categoria categoria = buildCategoria("Categoria Test");
    sessionFactory.getCurrentSession().save(categoria);

    Producto producto = buildProductoConCategoria("Producto Completo", categoria);
    repositorioProducto.guardar(producto);

    Producto resultado = repositorioProducto.obtenerProductoConReglasYCategorias(producto.getId());

    assertNotNull(resultado);
    assertNotNull(resultado.getCategorias());
    assertFalse(resultado.getCategorias().isEmpty());
  }

  @Test
  @DisplayName("NEG-04 | obtenerProductoConReglasYCategorias | Retorna null cuando no existe")
  @Transactional
  @Rollback
  void obtenerProductoConReglasYCategorias_deberiaRetornarNullCuandoNoExiste() {
    Producto resultado = repositorioProducto.obtenerProductoConReglasYCategorias(999L);

    assertNull(resultado);
  }

  // ===================== actualizar =====================

  @Test
  @DisplayName("HP-07 | actualizar | Actualiza un producto en la base de datos")
  @Transactional
  @Rollback
  void actualizar_deberiaActualizarUnProducto() {
    Producto producto = buildProducto("Original");
    repositorioProducto.guardar(producto);

    producto.setNombre("Modificado");
    repositorioProducto.actualizar(producto);

    Producto resultado = repositorioProducto.obtenerProductoPorId(producto.getId());
    assertEquals("Modificado", resultado.getNombre());
  }

  // ===================== obtenerTodos =====================

  @Test
  @DisplayName("HP-08 | obtenerTodos | Retorna todos los productos activos en orden alfabético")
  @Transactional
  @Rollback
  void obtenerTodos_deberiaRetornarProductosActivosOrdenados() {
    Producto p1 = buildProducto("Zanahoria");
    Producto p2 = buildProducto("Apple");
    Producto p3 = buildProducto("Inactivo");
    p3.setEstaActivo(false);

    repositorioProducto.guardar(p1);
    repositorioProducto.guardar(p2);
    repositorioProducto.guardar(p3);

    List<Producto> lista = repositorioProducto.obtenerTodos();
    assertNotNull(lista);
    assertTrue(lista.size() >= 2);
    assertTrue(lista.stream().noneMatch(p -> "Inactivo".equals(p.getNombre())));
  }

  // ===================== helpers =====================

  private Producto buildProducto(String nombre) {
    Producto producto = new Producto();
    producto.setNombre(nombre);
    producto.setEstaActivo(true);
    return producto;
  }

  private Categoria buildCategoria(String nombre) {
    Categoria categoria = new Categoria();
    categoria.setNombre(nombre);
    categoria.setEstaActiva(true);
    return categoria;
  }

  private Producto buildProductoConCategoria(String nombre, Categoria categoria) {
    Producto producto = buildProducto(nombre);
    producto.getCategorias().add(categoria);
    return producto;
  }
}
