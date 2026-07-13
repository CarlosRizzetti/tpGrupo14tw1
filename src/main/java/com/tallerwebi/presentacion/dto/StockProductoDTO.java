package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.Lote;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockProductoDTO {

  private String nombreProducto;
  private Integer stockTotal;
  private Lote loteEnUso;
  private List<Lote> lotesDisponibles;
}
