package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.*;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.dominio.interfaces.RepositorioComanda;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.dominio.services.ServicioComandaImpl;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioComandaImplTest {

  private RepositorioComanda repositorioComanda;
  private ServicioTimer servicioTimer;
  private ServicioComandaImpl servicioComanda;

  @BeforeEach
  public void setUp() {
    repositorioComanda = mock(RepositorioComanda.class);
    servicioTimer = mock(ServicioTimer.class);
    servicioComanda = new ServicioComandaImpl(repositorioComanda, servicioTimer);
  }

  @Test
  public void queSePuedaListarPendientesPorCategoria() {
    Comanda comanda1 = new Comanda();
    comanda1.setId(1L);
    comanda1.setEstado(EstadoComanda.PENDIENTE);

    Pedido pedido = new Pedido();
    pedido.setId(1L);
    comanda1.setPedido(pedido);

    List<Comanda> comandas = Arrays.asList(comanda1);
    when(repositorioComanda.listarPendientesPorCategoria(1L)).thenReturn(comandas);

    List<ComandaCocinaDTO> resultado = servicioComanda.listarPendientesPorCategoria(1L);

    assertThat(resultado.size(), equalTo(1));
    assertThat(resultado.get(0).getId(), equalTo(1L));
  }

  @Test
  public void queSePuedaContarPendientesPorCategoria() {
    Comanda comanda1 = new Comanda();
    List<Comanda> comandas = Arrays.asList(comanda1, comanda1);
    when(repositorioComanda.listarPendientesPorCategoria(1L)).thenReturn(comandas);

    int cantidad = servicioComanda.contarPendientesPorCategoria(1L);

    assertThat(cantidad, equalTo(2));
  }

  @Test
  public void queSePuedaSacarComandaExitosamente() throws IngredientesNoDisponiblesException {
    Comanda comanda = new Comanda();
    comanda.setId(1L);
    comanda.setEstado(EstadoComanda.PENDIENTE);

    Pedido pedido = new Pedido();
    pedido.setId(1L);
    pedido.setDetalles(new ArrayList<>());
    comanda.setPedido(pedido);

    DetallePedido detalle = new DetallePedido();
    detalle.setIngredientes(new ArrayList<>());
    pedido.getDetalles().add(detalle);

    DetallePedidoIngrediente ingrediente = new DetallePedidoIngrediente();
    ingrediente.setCantidad(2);
    Producto producto = new Producto();
    producto.setId(10L);
    ingrediente.setProducto(producto);
    ingrediente.setConsumos(new ArrayList<>());
    detalle.getIngredientes().add(ingrediente);

    when(repositorioComanda.buscarPorId(1L)).thenReturn(comanda);

    Timer timer = new Timer();
    timer.setId(1L);
    timer.setCantidadProducto(5);
    List<Timer> timers = Arrays.asList(timer);
    when(servicioTimer.obtenerTimersActivosConStockPorProducto(10L)).thenReturn(timers);

    servicioComanda.sacarComanda(1L);

    assertThat(comanda.getEstado(), equalTo(EstadoComanda.SACADA));
    assertThat(pedido.getEstado(), equalTo(EstadoPedido.ENTREGADO));
    verify(servicioTimer, times(1)).descontarStock(timer.getId(), 2);
    assertThat(ingrediente.getConsumos().size(), equalTo(1));
    verify(repositorioComanda, times(1)).actualizar(comanda);
  }

  @Test
  public void queLanceExcepcionAlSacarComandaSinStockSuficiente() {
    Comanda comanda = new Comanda();
    comanda.setId(1L);
    comanda.setEstado(EstadoComanda.PENDIENTE);

    Pedido pedido = new Pedido();
    pedido.setId(1L);
    pedido.setDetalles(new ArrayList<>());
    comanda.setPedido(pedido);

    DetallePedido detalle = new DetallePedido();
    detalle.setIngredientes(new ArrayList<>());
    pedido.getDetalles().add(detalle);

    DetallePedidoIngrediente ingrediente = new DetallePedidoIngrediente();
    ingrediente.setCantidad(5);
    Producto producto = new Producto();
    producto.setId(10L);
    ingrediente.setProducto(producto);
    detalle.getIngredientes().add(ingrediente);

    when(repositorioComanda.buscarPorId(1L)).thenReturn(comanda);

    Timer timer = new Timer();
    timer.setId(1L);
    timer.setCantidadProducto(3);
    List<Timer> timers = Arrays.asList(timer);
    when(servicioTimer.obtenerTimersActivosConStockPorProducto(10L)).thenReturn(timers);

    assertThrows(
      IngredientesNoDisponiblesException.class,
      () -> {
        servicioComanda.sacarComanda(1L);
      }
    );
  }

  @Test
  public void queLanceExcepcionAlSacarComandaSinNingunTimer() {
    Comanda comanda = new Comanda();
    comanda.setId(1L);
    comanda.setEstado(EstadoComanda.PENDIENTE);

    Pedido pedido = new Pedido();
    pedido.setId(1L);
    pedido.setDetalles(new ArrayList<>());
    comanda.setPedido(pedido);

    DetallePedido detalle = new DetallePedido();
    detalle.setIngredientes(new ArrayList<>());
    pedido.getDetalles().add(detalle);

    DetallePedidoIngrediente ingrediente = new DetallePedidoIngrediente();
    ingrediente.setCantidad(2);
    Producto producto = new Producto();
    producto.setId(10L);
    ingrediente.setProducto(producto);
    detalle.getIngredientes().add(ingrediente);

    when(repositorioComanda.buscarPorId(1L)).thenReturn(comanda);
    when(servicioTimer.obtenerTimersActivosConStockPorProducto(10L)).thenReturn(new ArrayList<>());

    assertThrows(
      IngredientesNoDisponiblesException.class,
      () -> {
        servicioComanda.sacarComanda(1L);
      }
    );
  }
}
