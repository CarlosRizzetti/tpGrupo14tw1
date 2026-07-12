package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.enums.TipoProducto;
import com.tallerwebi.dominio.entity.enums.UnidadDeMedida;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoDto {

  private String nombre;
  private List<Long> categoriasIds;
  private String ubicacion;
  private Integer duracionMinutos;
  private Boolean tieneDescongelamiento;
  private Integer descongelamientoMinutos;
  private TipoProducto tipoProducto;
  private UnidadDeMedida unidadDeMedida;
}
