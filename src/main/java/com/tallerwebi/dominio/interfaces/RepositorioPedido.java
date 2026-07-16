package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Cliente;
import java.util.List;

public interface RepositorioPedido {
  void guardar(Pedido pedido);

  Pedido buscarPorId(Long id);

  List<Pedido> listarTodos();

  List<Pedido> listarPorCliente(Long idCliente);

  List<Pedido> buscarPorCliente(Cliente cliente);

  List<Pedido> buscarPedidosReportados();
}
