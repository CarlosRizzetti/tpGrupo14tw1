package com.tallerwebi.presentacion.dto;

// Ajustá el paquete si tus DTOs de test viven en otro lado.

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.utils.ItemCarrito;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ItemCarritoDTOTest {

  @Test
  @DisplayName(
    "HP-01 | constructor | debería mapear idLinea, nombre y precio desde el producto final"
  )
  void constructorMapeaCamposSimplesDelProductoFinal() {
    ProductoFinal productoFinal = crearProductoFinal(
      "Hamburguesa Completa",
      new BigDecimal("2500.00")
    );
    ItemCarrito item = new ItemCarrito(productoFinal);

    ItemCarritoDTO dto = new ItemCarritoDTO(3L, item);

    assertThat(dto.getIdLinea(), equalTo(3L));
    assertThat(dto.getNombre(), equalTo("Hamburguesa Completa"));
    assertThat(dto.getPrecio(), equalTo(new BigDecimal("2500.00")));
  }

  @Test
  @DisplayName(
    "HP-02 | constructor | debería mapear los ingredientes con su producto, nombre y cantidad"
  )
  void constructorMapeaLosIngredientesDelItem() {
    ProductoFinal productoFinal = crearProductoFinal("Hamburguesa Completa", BigDecimal.TEN);
    Producto queso = crearProducto(1L, "Queso");
    Producto cebolla = crearProducto(2L, "Cebolla");
    productoFinal.agregarIngrediente(queso, 2);
    productoFinal.agregarIngrediente(cebolla, 1);
    ItemCarrito item = new ItemCarrito(productoFinal);

    ItemCarritoDTO dto = new ItemCarritoDTO(1L, item);

    assertThat(dto.getIngredientes(), hasSize(2));
    ItemCarritoDTO.IngredienteEnItemDto ingredienteQueso = dto.getIngredientes().get(0);
    assertThat(ingredienteQueso.getProductoId(), equalTo(1L));
    assertThat(ingredienteQueso.getNombre(), equalTo("Queso"));
    assertThat(ingredienteQueso.getCantidad(), equalTo(2));
    ItemCarritoDTO.IngredienteEnItemDto ingredienteCebolla = dto.getIngredientes().get(1);
    assertThat(ingredienteCebolla.getNombre(), equalTo("Cebolla"));
    assertThat(ingredienteCebolla.getCantidad(), equalTo(1));
  }

  @Test
  @DisplayName(
    "EDGE-01 | constructor | debería devolver ingredientes vacío si el producto final no tiene receta"
  )
  void constructorDevuelveIngredientesVacioSiElProductoFinalNoTieneReceta() {
    ProductoFinal productoFinal = crearProductoFinal("Café", BigDecimal.ONE);
    ItemCarrito item = new ItemCarrito(productoFinal);

    ItemCarritoDTO dto = new ItemCarritoDTO(1L, item);

    assertThat(dto.getIngredientes(), empty());
  }

  @Test
  @DisplayName(
    "EDGE-02 | constructor | debería reflejar la cantidad ACTUAL del ingrediente (no la original) si se retiró alguna unidad"
  )
  void constructorReflejaLaCantidadActualNoLaOriginal() {
    ProductoFinal productoFinal = crearProductoFinal("Hamburguesa Completa", BigDecimal.TEN);
    Producto queso = crearProducto(1L, "Queso");
    productoFinal.agregarIngrediente(queso, 3);
    ItemCarrito item = new ItemCarrito(productoFinal);
    item.retirarIngrediente(1L);

    ItemCarritoDTO dto = new ItemCarritoDTO(1L, item);

    assertThat(dto.getIngredientes().get(0).getCantidad(), equalTo(2));
  }

  @Test
  @DisplayName(
    "EDGE-03 | constructor | debería seguir listando un ingrediente retirado del todo, con cantidad en cero"
  )
  void constructorMantieneEnLaListaAlIngredienteRetiradoDelTodo() {
    ProductoFinal productoFinal = crearProductoFinal("Hamburguesa Completa", BigDecimal.TEN);
    Producto queso = crearProducto(1L, "Queso");
    productoFinal.agregarIngrediente(queso, 1);
    ItemCarrito item = new ItemCarrito(productoFinal);
    item.retirarIngrediente(1L);

    ItemCarritoDTO dto = new ItemCarritoDTO(1L, item);

    assertThat(dto.getIngredientes(), hasSize(1));
    assertThat(dto.getIngredientes().get(0).getCantidad(), equalTo(0));
  }

  private Producto crearProducto(Long id, String nombre) {
    Producto producto = new Producto();
    producto.setId(id);
    producto.setNombre(nombre);
    return producto;
  }

  private ProductoFinal crearProductoFinal(String nombre, BigDecimal precio) {
    return new ProductoFinal(nombre, precio);
  }
}
