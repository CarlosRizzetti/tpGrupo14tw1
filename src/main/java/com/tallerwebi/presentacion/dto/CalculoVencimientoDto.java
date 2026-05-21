package com.tallerwebi.presentacion.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculoVencimientoDto {

  private LocalDateTime tiempoElaboracion;
  private LocalDateTime tiempoVencimiento;
  private LocalDateTime tiempoDescongelamiento;
  private String nombreProducto;
  private String ubicacion;
}
