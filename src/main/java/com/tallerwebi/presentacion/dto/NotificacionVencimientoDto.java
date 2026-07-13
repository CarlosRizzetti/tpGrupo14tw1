package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.Lote;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class NotificacionVencimientoDto {

  private Lote lote;
  private long diasRestantes;
  private String nivelUrgencia;
}
