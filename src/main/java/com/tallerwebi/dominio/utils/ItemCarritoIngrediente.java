package com.tallerwebi.dominio.utils;

import com.tallerwebi.dominio.entity.Producto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCarritoIngrediente {

  private Producto producto;
  private int cantidadOriginal;
  private int cantidadActual;

  public ItemCarritoIngrediente(Producto producto, int cantidadOriginal) {
    this.producto = producto;
    this.cantidadOriginal = cantidadOriginal;
    this.cantidadActual = cantidadOriginal;
  }

  public void retirarUnidad() {
    if (cantidadActual > 0) {
      cantidadActual--;
    }
  }

  public boolean fueRetiradoDelTodo() {
    return cantidadActual == 0;
  }
}
