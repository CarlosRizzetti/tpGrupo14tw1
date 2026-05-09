package com.tallerwebi.presentacion;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatosProducto {

  private String nombre;
  private List<Long> categoriasIds; // IDs de las categorías seleccionadas
  private String ubicacion;
  private Integer duracionMinutos;
  private Boolean tieneDescongelamiento;
  private Integer descongelamientoMinutos;
}
