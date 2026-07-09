package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.utils.CarritoPedido;

public interface ServicioPedido {
  Pedido cobrarPedido(CarritoPedido carrito, Cliente cliente);
}
