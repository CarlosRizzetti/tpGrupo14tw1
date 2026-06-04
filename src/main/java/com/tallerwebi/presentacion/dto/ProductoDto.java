package com.tallerwebi.presentacion.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoDto {

  private String nombre;
  private List<Long> categoriasIds;
  private Integer cantidad;
  private String ubicacion;
  private Integer duracionMinutos;
  private Boolean tieneDescongelamiento;
  private Integer descongelamientoMinutos;
}
