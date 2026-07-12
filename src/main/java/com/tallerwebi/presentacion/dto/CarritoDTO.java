package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.utils.CarritoPedido;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarritoDTO {

  private List<ItemCarritoDTO> items;
  private BigDecimal total;

  public CarritoDTO(CarritoPedido carrito) {
    List<ItemCarritoDTO> lista = new ArrayList<>();
    for (int i = 0; i < carrito.getItems().size(); i++) {
      lista.add(new ItemCarritoDTO((long) i, carrito.getItems().get(i)));
    }
    this.items = lista;
    this.total = carrito.calcularTotal();
  }
}
