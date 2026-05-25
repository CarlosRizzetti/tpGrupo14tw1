package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.Categoria;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaDto {

  private Long id;

  private String nombre;
  private String icono;
  private String tema;

  public CategoriaDto(Categoria categoria) {
    this.id = categoria.getId();
    this.nombre = categoria.getNombre();
    this.icono = categoria.getIcono();
    String nombreMinusculas = categoria.getNombre().toLowerCase(Locale.ROOT);
    if (nombreMinusculas.contains("caf")) {
      this.tema = "tema-mccafe";
    } else if (nombreMinusculas.contains("serv")) {
      this.tema = "tema-servicio";
    } else if (nombreMinusculas.contains("isla")) {
      this.tema = "tema-isla";
    } else {
      this.tema = "tema-cocina";
    }
  }
}
