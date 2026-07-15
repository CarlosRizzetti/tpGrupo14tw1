package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.DetallePedidoIngrediente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import com.tallerwebi.dominio.interfaces.RepositorioPedido;
import com.tallerwebi.dominio.interfaces.ServicioPedido;
import com.tallerwebi.dominio.utils.CarritoPedido;
import com.tallerwebi.dominio.utils.ItemCarrito;
import com.tallerwebi.dominio.utils.ItemCarritoIngrediente;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioPedido")
public class ServicioPedidoImpl implements ServicioPedido {

  private final RepositorioPedido repositorioPedido;

  @Autowired
  public ServicioPedidoImpl(RepositorioPedido repositorioPedido) {
    this.repositorioPedido = repositorioPedido;
  }

  /**
   * Orquestador: arma el pedido a partir del carrito, lo persiste y limpia el carrito.
   */
  @Override
  @Transactional
  public Pedido cobrarPedido(CarritoPedido carrito, Cliente cliente) {
    Pedido pedido = crearCabeceraPedido(cliente, carrito);
    agregarDetallesAlPedido(pedido, carrito);
    generarComanda(pedido);
    persistirYLimpiar(pedido, carrito);
    return pedido;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidosDeCliente(Cliente cliente) {
    if (cliente == null) {
      return Collections.emptyList();
    }
    List<Pedido> pedidos = repositorioPedido.buscarPorCliente(cliente);
    for (Pedido pedido : pedidos) {
      if (pedido.getDetalles() != null) {
        pedido.getDetalles().size();
        for (DetallePedido detalle : pedido.getDetalles()) {
          if (detalle.getProductoFinal() != null) {
            detalle.getProductoFinal().getNombre();
          }
          if (detalle.getIngredientes() != null) {
            detalle.getIngredientes().size();
            for (DetallePedidoIngrediente ingrediente : detalle.getIngredientes()) {
              if (ingrediente.getProducto() != null) {
                ingrediente.getProducto().getNombre();
              }
            }
          }
        }
      }
    }
    return pedidos;
  }

  @Override
  @Transactional(readOnly = true)
  public Pedido buscarPedidoPorId(Long id) {
    if (id == null) {
      return null;
    }
    Pedido pedidoEncontrado = repositorioPedido.buscarPorId(id);
    if (pedidoEncontrado != null && pedidoEncontrado.getDetalles() != null) {
      pedidoEncontrado.getDetalles().size();
      for (DetallePedido detalle : pedidoEncontrado.getDetalles()) {
        if (detalle.getProductoFinal() != null) {
          detalle.getProductoFinal().getNombre();
        }
        if (detalle.getIngredientes() != null) {
          detalle.getIngredientes().size();
          for (DetallePedidoIngrediente ingrediente : detalle.getIngredientes()) {
            if (ingrediente.getProducto() != null) {
              ingrediente.getProducto().getNombre();
            }
          }
        }
      }
    }
    return pedidoEncontrado;
  }

  @Override
  @Transactional
  public void marcarPedidoComoReportado(Long idPedido) {
    if (idPedido == null) {
      return;
    }
    Pedido pedidoEncontrado = repositorioPedido.buscarPorId(idPedido);
    if (pedidoEncontrado != null) {
      pedidoEncontrado.setReportado(true);
      repositorioPedido.guardar(pedidoEncontrado);
    }
  }

  private Pedido crearCabeceraPedido(Cliente cliente, CarritoPedido carrito) {
    Pedido pedido = new Pedido();
    pedido.setCliente(cliente);
    pedido.setHoraCobro(OffsetDateTime.now());
    pedido.setPrecioFinal(carrito.calcularTotal());
    pedido.setEstado(EstadoPedido.EN_COCINA);
    return pedido;
  }

  private void agregarDetallesAlPedido(Pedido pedido, CarritoPedido carrito) {
    for (ItemCarrito itemCarrito : carrito.getItems()) {
      DetallePedido detalle = crearDetalleDesdeItem(itemCarrito, pedido);
      pedido.getDetalles().add(detalle);
    }
  }

  private DetallePedido crearDetalleDesdeItem(ItemCarrito itemCarrito, Pedido pedido) {
    DetallePedido detalle = new DetallePedido();
    detalle.setPedido(pedido);
    detalle.setProductoFinal(itemCarrito.getProductoFinal());

    for (ItemCarritoIngrediente ingredienteCarrito : itemCarrito.getIngredientes()) {
      DetallePedidoIngrediente ingrediente = crearIngredienteSiCorresponde(
        ingredienteCarrito,
        detalle
      );
      if (ingrediente != null) {
        detalle.getIngredientes().add(ingrediente);
      }
    }
    return detalle;
  }

  private DetallePedidoIngrediente crearIngredienteSiCorresponde(
    ItemCarritoIngrediente ingredienteCarrito,
    DetallePedido detalle
  ) {
    if (ingredienteCarrito.fueRetiradoDelTodo()) {
      return null;
    }
    DetallePedidoIngrediente ingrediente = new DetallePedidoIngrediente();
    ingrediente.setDetallePedido(detalle);
    ingrediente.setProducto(ingredienteCarrito.getProducto());
    ingrediente.setCantidad(ingredienteCarrito.getCantidadActual());
    return ingrediente;
  }

  /**
   * Genera la Comanda inicial (PENDIENTE) y la asocia al Pedido en ambas direcciones.
   */
  private void generarComanda(Pedido pedido) {
    Comanda comanda = new Comanda();
    comanda.setPedido(pedido);
    comanda.setEstado(EstadoComanda.PENDIENTE);
    pedido.setComanda(comanda);
  }

  /**
   * Persiste el pedido (cascade hace el resto) y vacía el carrito de sesión.
   */
  private void persistirYLimpiar(Pedido pedido, CarritoPedido carrito) {
    repositorioPedido.guardar(pedido);
    carrito.vaciar();
  }
}
