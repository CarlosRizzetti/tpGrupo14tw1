package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.RepositorioProductoFinal;
import com.tallerwebi.dominio.services.ServicioProductoFinalImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServicioProductoFinalTest {

  private RepositorioProductoFinal repositorioProductoFinal;
  private ServicioProductoFinalImpl servicio;

  @BeforeEach
  public void init() {
    repositorioProductoFinal = mock(RepositorioProductoFinal.class);
    servicio = new ServicioProductoFinalImpl(repositorioProductoFinal);
  }

  @Test
  @DisplayName("HP-01 | listarTodos | Delega en el repositorio y devuelve la lista completa")
  public void listarTodosDeberiaDelegarEnElRepositorio() {
    ProductoFinal p1 = new ProductoFinal();
    ProductoFinal p2 = new ProductoFinal();
    when(repositorioProductoFinal.listarTodos()).thenReturn(Arrays.asList(p1, p2));

    List<ProductoFinal> resultado = servicio.listarTodos();

    assertEquals(2, resultado.size());
    assertEquals(Arrays.asList(p1, p2), resultado);
    verify(repositorioProductoFinal, times(1)).listarTodos();
  }

  @Test
  @DisplayName(
    "EDGE-01 | listarTodos | Devuelve lista vacía si el repositorio no tiene productos finales"
  )
  public void listarTodosSinDatosDeberiaDevolverListaVacia() {
    when(repositorioProductoFinal.listarTodos()).thenReturn(new ArrayList<>());

    List<ProductoFinal> resultado = servicio.listarTodos();

    assertTrue(resultado.isEmpty());
  }

  @Test
  @DisplayName("HP-02 | buscarPorId | Delega en el repositorio y devuelve el producto encontrado")
  public void buscarPorIdDeberiaDelegarEnElRepositorio() {
    ProductoFinal productoFinal = new ProductoFinal();
    when(repositorioProductoFinal.buscarPorId(1L)).thenReturn(productoFinal);

    ProductoFinal resultado = servicio.buscarPorId(1L);

    assertEquals(productoFinal, resultado);
    verify(repositorioProductoFinal, times(1)).buscarPorId(1L);
  }

  @Test
  @DisplayName("EDGE-02 | buscarPorId | Devuelve null si el repositorio no encuentra el producto")
  public void buscarPorIdInexistenteDeberiaDevolverNull() {
    when(repositorioProductoFinal.buscarPorId(99L)).thenReturn(null);

    ProductoFinal resultado = servicio.buscarPorId(99L);

    assertNull(resultado);
  }

  @Test
  @DisplayName("HP-03 | listarPorCategoria | Delega en el repositorio y devuelve la lista filtrada")
  public void listarPorCategoriaDeberiaDelegarEnElRepositorio() {
    ProductoFinal productoFinal = new ProductoFinal();
    when(repositorioProductoFinal.listarPorCategoria(3L)).thenReturn(Arrays.asList(productoFinal));

    List<ProductoFinal> resultado = servicio.listarPorCategoria(3L);

    assertEquals(1, resultado.size());
    assertEquals(productoFinal, resultado.get(0));
    verify(repositorioProductoFinal, times(1)).listarPorCategoria(3L);
  }

  @Test
  @DisplayName(
    "EDGE-03 | listarPorCategoria | Devuelve lista vacía si no hay productos finales en esa categoría"
  )
  public void listarPorCategoriaSinResultadosDeberiaDevolverListaVacia() {
    when(repositorioProductoFinal.listarPorCategoria(5L)).thenReturn(new ArrayList<>());

    List<ProductoFinal> resultado = servicio.listarPorCategoria(5L);

    assertTrue(resultado.isEmpty());
  }
}
