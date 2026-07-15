package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Pedido;
import java.util.List;

public interface RepositorioPedido {
  void guardar(Pedido pedido);

  Pedido buscarPorId(Long id);

  List<Pedido> listarTodos();

  List<Pedido> listarPorCliente(Long idCliente);
}
