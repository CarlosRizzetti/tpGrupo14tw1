package com.tallerwebi.presentacion.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.DetallePedidoIngrediente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ProductoFinal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ComandaCocinaDTOTest {

  @Test
  @DisplayName(
    "HP-01 | constructor | debería mapear id, idPedido y horaCobro desde la comanda y su pedido"
  )
  void constructorMapeaCamposSimplesDeLaComandaYElPedido() {
    OffsetDateTime horaCobro = OffsetDateTime.of(2026, 7, 16, 12, 30, 0, 0, ZoneOffset.ofHours(-3));
    Pedido pedido = crearPedido(1L, horaCobro, new ArrayList<>());
    Comanda comanda = crearComanda(5L, pedido);

    ComandaCocinaDTO dto = new ComandaCocinaDTO(comanda);

    assertThat(dto.getId(), equalTo(comanda.getId()));
    assertThat(dto.getIdPedido(), equalTo(pedido.getId()));
    assertThat(dto.getHoraCobro(), equalTo(horaCobro.toString()));
  }

  @Test
  @DisplayName("HP-02 | constructor | debería mapear una línea con su nombre y sus ingredientes")
  void constructorMapeaUnaLineaConSusIngredientes() {
    DetallePedidoIngrediente queso = crearIngrediente("Queso", 2);
    DetallePedidoIngrediente cebolla = crearIngrediente("Cebolla", 1);
    DetallePedido detalle = crearDetalle("Hamburguesa Completa", Arrays.asList(queso, cebolla));
    Pedido pedido = crearPedido(1L, OffsetDateTime.now(), List.of(detalle));
    Comanda comanda = crearComanda(5L, pedido);

    ComandaCocinaDTO dto = new ComandaCocinaDTO(comanda);

    assertThat(dto.getLineas(), hasSize(1));
    ComandaCocinaDTO.LineaDto linea = dto.getLineas().get(0);
    assertThat(linea.getNombre(), equalTo("Hamburguesa Completa"));
    assertThat(linea.isTieneIngredientes(), is(true));
    assertThat(linea.getIngredientes(), hasSize(2));
    assertThat(linea.getIngredientes().get(0).getNombre(), equalTo("Queso"));
    assertThat(linea.getIngredientes().get(0).getCantidad(), equalTo(2));
    assertThat(linea.getIngredientes().get(1).getNombre(), equalTo("Cebolla"));
    assertThat(linea.getIngredientes().get(1).getCantidad(), equalTo(1));
  }

  @Test
  @DisplayName("HP-03 | constructor | debería mapear varias líneas manteniendo el orden del pedido")
  void constructorMapeaVariasLineasEnOrden() {
    DetallePedido detalleUno = crearDetalle("Hamburguesa Completa", List.of());
    DetallePedido detalleDos = crearDetalle("Papas Fritas", List.of());
    Pedido pedido = crearPedido(1L, OffsetDateTime.now(), Arrays.asList(detalleUno, detalleDos));
    Comanda comanda = crearComanda(5L, pedido);

    ComandaCocinaDTO dto = new ComandaCocinaDTO(comanda);

    assertThat(dto.getLineas(), hasSize(2));
    assertThat(dto.getLineas().get(0).getNombre(), equalTo("Hamburguesa Completa"));
    assertThat(dto.getLineas().get(1).getNombre(), equalTo("Papas Fritas"));
  }

  @Test
  @DisplayName(
    "EDGE-01 | constructor | debería marcar tieneIngredientes en false y dejar la lista vacía si el detalle no tiene ingredientes"
  )
  void constructorMarcaTieneIngredientesEnFalseSiNoHayIngredientes() {
    DetallePedido detalle = crearDetalle("Café", List.of());
    Pedido pedido = crearPedido(1L, OffsetDateTime.now(), List.of(detalle));
    Comanda comanda = crearComanda(5L, pedido);

    ComandaCocinaDTO dto = new ComandaCocinaDTO(comanda);

    ComandaCocinaDTO.LineaDto linea = dto.getLineas().get(0);
    assertThat(linea.isTieneIngredientes(), is(false));
    assertThat(linea.getIngredientes(), empty());
  }

  @Test
  @DisplayName(
    "EDGE-02 | constructor | debería dejar horaCobro en null si el pedido todavía no fue cobrado"
  )
  void constructorDejaHoraCobroEnNullSiElPedidoNoTieneHoraCobro() {
    Pedido pedido = crearPedido(1L, null, new ArrayList<>());
    Comanda comanda = crearComanda(5L, pedido);

    ComandaCocinaDTO dto = new ComandaCocinaDTO(comanda);

    assertThat(dto.getHoraCobro(), nullValue());
  }

  @Test
  @DisplayName(
    "EDGE-03 | constructor | debería devolver lineas vacía si el pedido no tiene detalles"
  )
  void constructorDevuelveLineasVaciaSiElPedidoNoTieneDetalles() {
    Pedido pedido = crearPedido(1L, OffsetDateTime.now(), new ArrayList<>());
    Comanda comanda = crearComanda(5L, pedido);

    ComandaCocinaDTO dto = new ComandaCocinaDTO(comanda);

    assertThat(dto.getLineas(), empty());
  }

  private Comanda crearComanda(Long id, Pedido pedido) {
    Comanda comanda = new Comanda();
    comanda.setId(id);
    comanda.setPedido(pedido);
    return comanda;
  }

  private Pedido crearPedido(Long id, OffsetDateTime horaCobro, List<DetallePedido> detalles) {
    Pedido pedido = new Pedido();
    pedido.setId(id);
    pedido.setHoraCobro(horaCobro);
    pedido.setDetalles(detalles);
    return pedido;
  }

  private DetallePedido crearDetalle(
    String nombreProductoFinal,
    List<DetallePedidoIngrediente> ingredientes
  ) {
    ProductoFinal productoFinal = new ProductoFinal();
    productoFinal.setNombre(nombreProductoFinal);
    DetallePedido detalle = new DetallePedido();
    detalle.setProductoFinal(productoFinal);
    detalle.setIngredientes(new ArrayList<>(ingredientes));
    return detalle;
  }

  private DetallePedidoIngrediente crearIngrediente(String nombreProducto, Integer cantidad) {
    Producto producto = new Producto();
    producto.setNombre(nombreProducto);
    DetallePedidoIngrediente ingrediente = new DetallePedidoIngrediente();
    ingrediente.setProducto(producto);
    ingrediente.setCantidad(cantidad);
    return ingrediente;
  }
}
