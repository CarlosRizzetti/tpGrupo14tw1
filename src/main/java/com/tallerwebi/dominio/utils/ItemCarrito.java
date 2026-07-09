package com.tallerwebi.dominio.utils;

import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.entity.ProductoFinalIngrediente;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCarrito {

  private ProductoFinal productoFinal;
  private List<ItemCarritoIngrediente> ingredientes;

  public ItemCarrito(ProductoFinal productoFinal) {
    this.productoFinal = productoFinal;
    this.ingredientes = new ArrayList<>();
    for (ProductoFinalIngrediente pfi : productoFinal.getIngredientes()) {
      this.ingredientes.add(new ItemCarritoIngrediente(pfi.getProducto(), pfi.getCantidad()));
    }
  }

  public void retirarIngrediente(Long productoId) {
    ingredientes
      .stream()
      .filter(i -> i.getProducto().getId().equals(productoId))
      .findFirst()
      .ifPresent(ItemCarritoIngrediente::retirarUnidad);
  }
}
