package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.ProductoFinalIngrediente;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredienteDTO {

  private Long productoId;
  private String nombre;
  private Integer cantidad;

  public IngredienteDTO(ProductoFinalIngrediente pfi) {
    this.productoId = pfi.getProducto().getId();
    this.nombre = pfi.getProducto().getNombre();
    this.cantidad = pfi.getCantidad();
  }
}
