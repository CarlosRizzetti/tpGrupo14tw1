package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.utils.CarritoPedido;
import java.util.List;

public interface ServicioPedido {
  Pedido cobrarPedido(CarritoPedido carrito, Cliente cliente);

  List<Pedido> listarPedidosDeCliente(Cliente cliente);

  Pedido buscarPedidoPorId(Long id);

  void marcarPedidoComoReportado(Long idPedido);
}
