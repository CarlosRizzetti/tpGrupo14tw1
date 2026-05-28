package com.tallerwebi.presentacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimerDTO {

  private Long id;
  private String nombre;
  private String groupId;
  private String fechaCreacion;
  private String fechaVencimiento;
  private String ubicacion;
}
