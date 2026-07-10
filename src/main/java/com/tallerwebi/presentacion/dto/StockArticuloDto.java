package com.tallerwebi.presentacion.dto;

/**
 * DTO para transportar el nombre del articulo y su stock total.
 */
public class StockArticuloDto {

  private String nombre;
  private Double stock;

  public StockArticuloDto() {}

  public StockArticuloDto(String nombre, Double stock) {
    this.nombre = nombre;
    this.stock = stock;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Double getStock() {
    return stock;
  }

  public void setStock(Double stock) {
    this.stock = stock;
  }
}
