package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.ProductoFinal;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoFinalDTO {

  private Long id;
  private String nombre;
  private BigDecimal precio;
  private boolean tieneIngredientes;
  private List<IngredienteDTO> ingredientes;

  public ProductoFinalDTO(ProductoFinal pf) {
    this.id = pf.getId();
    this.nombre = pf.getNombre();
    this.precio = pf.getPrecio();
    this.tieneIngredientes = !pf.getIngredientes().isEmpty();
    this.ingredientes =
      pf.getIngredientes().stream().map(IngredienteDTO::new).collect(Collectors.toList());
  }
}
