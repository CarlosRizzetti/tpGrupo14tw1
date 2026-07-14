package com.tallerwebi.presentacion.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteUsadoDTO {

  private String nombreProducto;
  private Integer cantidad;
  private List<Long> timers;
}
