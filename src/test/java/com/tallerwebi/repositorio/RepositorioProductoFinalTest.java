package com.tallerwebi.repositorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.interfaces.RepositorioProductoFinal;
import com.tallerwebi.repositorio.config.HibernateInfraestructuraTestConfig;
import java.math.BigDecimal;
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
public class RepositorioProductoFinalTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioProductoFinal repositorioProductoFinal;
  private RepositorioProducto repositorioProducto;

  @BeforeEach
  public void init() {
    repositorioProductoFinal = new RepositorioProductoFinalImpl(sessionFactory);
    repositorioProducto = new RepositorioProductoImpl(sessionFactory);
  }

  // ---------- helpers ----------

  private Categoria crearYGuardarCategoria(String nombre) {
    Categoria categoria = new Categoria("icono.png", true, nombre);
    sessionFactory.getCurrentSession().save(categoria);
    return categoria;
  }

  private Producto crearYGuardarProducto(String nombre) {
    Producto producto = new Producto();
    producto.setNombre(nombre);
    producto.setEstaActivo(true);
    repositorioProducto.guardar(producto);
    return producto;
  }

  private ProductoFinal crearProductoFinal(String nombre, BigDecimal precio) {
    return new ProductoFinal(nombre, precio);
  }

  private ProductoFinal crearYGuardarProductoFinal(String nombre, BigDecimal precio) {
    ProductoFinal productoFinal = crearProductoFinal(nombre, precio);
    repositorioProductoFinal.guardar(productoFinal);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
    return productoFinal;
  }

  // ========================================================
  // buscarPorId
  // ========================================================

  @Test
  @DisplayName("HP-01 | buscarPorId | Devuelve el producto final correcto cuando existe")
  public void buscarPorIdDeberiaDevolverElProductoFinalCorrecto() {
    ProductoFinal productoFinal = crearYGuardarProductoFinal(
      "Hamburguesa Completa",
      new BigDecimal("2500.00")
    );

    ProductoFinal resultado = repositorioProductoFinal.buscarPorId(productoFinal.getId());

    assertNotNull(resultado);
    assertEquals("Hamburguesa Completa", resultado.getNombre());
  }

  @Test
  @DisplayName("NEG-01 | buscarPorId | Devuelve null cuando no existe un producto final con ese id")
  public void buscarPorIdDeberiaDevolverNullSiNoExiste() {
    ProductoFinal resultado = repositorioProductoFinal.buscarPorId(999999L);

    assertNull(resultado);
  }

  // ========================================================
  // listarTodos
  // ========================================================

  @Test
  @DisplayName("HP-02 | listarTodos | Devuelve todos los productos finales cargados")
  public void listarTodosDeberiaDevolverTodosLosProductosFinales() {
    crearYGuardarProductoFinal("Hamburguesa Completa", new BigDecimal("2500.00"));
    crearYGuardarProductoFinal("Papas Fritas", new BigDecimal("1200.00"));

    List<ProductoFinal> resultado = repositorioProductoFinal.listarTodos();

    assertEquals(2, resultado.size());
  }

  @Test
  @DisplayName(
    "EDGE-01 | listarTodos | Devuelve una lista vacía si no hay productos finales cargados"
  )
  public void listarTodosDeberiaDevolverListaVaciaSiNoHayNinguno() {
    List<ProductoFinal> resultado = repositorioProductoFinal.listarTodos();

    assertTrue(resultado.isEmpty());
  }

  // ========================================================
  // listarPorCategoria
  // ========================================================

  @Test
  @DisplayName(
    "HP-03 | listarPorCategoria | Devuelve solo los productos finales asociados a esa categoría"
  )
  public void listarPorCategoriaDeberiaDevolverSoloLosProductosFinalesDeEsaCategoria() {
    Categoria comidas = crearYGuardarCategoria("Comidas");
    Categoria bebidas = crearYGuardarCategoria("Bebidas");

    ProductoFinal hamburguesa = crearProductoFinal(
      "Hamburguesa Completa",
      new BigDecimal("2500.00")
    );
    hamburguesa.getCategorias().add(comidas);
    repositorioProductoFinal.guardar(hamburguesa);

    ProductoFinal gaseosa = crearProductoFinal("Gaseosa", new BigDecimal("800.00"));
    gaseosa.getCategorias().add(bebidas);
    repositorioProductoFinal.guardar(gaseosa);

    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    List<ProductoFinal> resultado = repositorioProductoFinal.listarPorCategoria(comidas.getId());

    assertEquals(1, resultado.size());
    assertEquals("Hamburguesa Completa", resultado.get(0).getNombre());
  }

  @Test
  @DisplayName(
    "EDGE-02 | listarPorCategoria | Devuelve una lista vacía si la categoría no tiene productos finales"
  )
  public void listarPorCategoriaDeberiaDevolverListaVaciaSiNoHayProductosAsociados() {
    Categoria comidas = crearYGuardarCategoria("Comidas");

    List<ProductoFinal> resultado = repositorioProductoFinal.listarPorCategoria(comidas.getId());

    assertTrue(resultado.isEmpty());
  }

  // ========================================================
  // guardar
  // ========================================================

  @Test
  @DisplayName("HP-04 | guardar | Persiste un producto final nuevo y le asigna un id")
  public void guardarProductoFinalNuevoDeberiaPersistirloYAsignarleId() {
    ProductoFinal productoFinal = crearProductoFinal(
      "Hamburguesa Completa",
      new BigDecimal("2500.00")
    );

    repositorioProductoFinal.guardar(productoFinal);
    sessionFactory.getCurrentSession().flush();

    assertNotNull(productoFinal.getId());
  }

  @Test
  @DisplayName("HP-05 | guardar | Persiste correctamente el nombre y el precio")
  public void guardarProductoFinalDeberiaPersistirNombreYPrecio() {
    ProductoFinal productoFinal = crearYGuardarProductoFinal(
      "Hamburguesa Completa",
      new BigDecimal("2500.00")
    );

    ProductoFinal resultado = sessionFactory
      .getCurrentSession()
      .get(ProductoFinal.class, productoFinal.getId());

    assertEquals("Hamburguesa Completa", resultado.getNombre());
    assertEquals(0, new BigDecimal("2500.00").compareTo(resultado.getPrecio()));
  }

  @Test
  @DisplayName("HP-06 | guardar | Persiste en cascada los ingredientes agregados al producto final")
  public void guardarProductoFinalDeberiaPersistirSusIngredientesEnCascada() {
    Producto queso = crearYGuardarProducto("Queso");
    ProductoFinal productoFinal = crearProductoFinal(
      "Hamburguesa Completa",
      new BigDecimal("2500.00")
    );
    productoFinal.agregarIngrediente(queso, 2);

    repositorioProductoFinal.guardar(productoFinal);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    ProductoFinal resultado = sessionFactory
      .getCurrentSession()
      .get(ProductoFinal.class, productoFinal.getId());

    assertEquals(1, resultado.getIngredientes().size());
    assertEquals("Queso", resultado.getIngredientes().get(0).getProducto().getNombre());
    assertEquals(2, resultado.getIngredientes().get(0).getCantidad());
  }
}
