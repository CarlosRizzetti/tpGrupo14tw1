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

  @Override
  @Transactional
  public Pedido cobrarPedido(CarritoPedido carrito, Cliente cliente) {
    Pedido pedido = new Pedido();

    pedido.setCliente(cliente);
    pedido.setHoraCobro(OffsetDateTime.now());
    pedido.setPrecioFinal(carrito.calcularTotal());
    pedido.setEstado(EstadoPedido.EN_COCINA);

    for (ItemCarrito itemCarrito : carrito.getItems()) {
      DetallePedido detalle = new DetallePedido();
      detalle.setPedido(pedido);
      detalle.setProductoFinal(itemCarrito.getProductoFinal());

      for (ItemCarritoIngrediente ingredienteCarrito : itemCarrito.getIngredientes()) {
        if (ingredienteCarrito.fueRetiradoDelTodo()) {
          continue;
        }
        DetallePedidoIngrediente detalleIngrediente = new DetallePedidoIngrediente();
        detalleIngrediente.setDetallePedido(detalle);
        detalleIngrediente.setProducto(ingredienteCarrito.getProducto());
        detalleIngrediente.setCantidad(ingredienteCarrito.getCantidadActual());
        detalle.getIngredientes().add(detalleIngrediente);
      }

      pedido.getDetalles().add(detalle);
    }

    Comanda comanda = new Comanda();
    comanda.setPedido(pedido);
    comanda.setEstado(EstadoComanda.PENDIENTE);
    pedido.setComanda(comanda);

    repositorioPedido.guardar(pedido);
    carrito.vaciar();

    return pedido;
  }
}
