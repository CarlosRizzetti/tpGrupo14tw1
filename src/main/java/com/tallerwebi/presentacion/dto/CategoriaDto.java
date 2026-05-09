package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.Categoria;
import java.util.Locale;

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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getIcono() {
    return icono;
  }

  public void setIcono(String icono) {
    this.icono = icono;
  }

  public String getTema() {
    return tema;
  }

  public void setTema(String tema) {
    this.tema = tema;
  }
}
