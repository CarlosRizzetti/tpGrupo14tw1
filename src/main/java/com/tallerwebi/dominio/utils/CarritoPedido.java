package com.tallerwebi.dominio.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CarritoPedido implements Serializable {

  private static final long serialVersionUID = 1L;
  private List<ItemCarrito> items = new ArrayList<>();

  public void agregarItem(ItemCarrito item) {
    items.add(item);
  }

  public boolean eliminarItem(int indice) {
    if (indice < 0 || indice >= items.size()) return false;
    items.remove(indice);
    return true;
  }

  public ItemCarrito obtenerItem(int indice) {
    if (indice < 0 || indice >= items.size()) return null;
    return items.get(indice);
  }

  public BigDecimal calcularTotal() {
    return items
      .stream()
      .map(item -> item.getProductoFinal().getPrecio())
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public boolean estaVacio() {
    return items.isEmpty();
  }

  public void vaciar() {
    items.clear();
  }

  public List<ItemCarrito> getItems() {
    return items;
  }
}
