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
public class ItemPedidoDTO {

  private String nombreProducto;
  private List<IngredienteUsadoDTO> ingredientes;
}
