package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Pedido;

public interface RepositorioPedido {
  void guardar(Pedido pedido);

  Pedido buscarPorId(Long id);
}
