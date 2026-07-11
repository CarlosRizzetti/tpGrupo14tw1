package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.Articulos;

public class NotificacionVencimientoDto {

  private Articulos articulo;
  private long diasRestantes;
  private String nivelUrgencia;

  public NotificacionVencimientoDto() {}

  public NotificacionVencimientoDto(Articulos articulo, long diasRestantes, String nivelUrgencia) {
    this.articulo = articulo;
    this.diasRestantes = diasRestantes;
    this.nivelUrgencia = nivelUrgencia;
  }

  public Articulos getArticulo() {
    return articulo;
  }

  public void setArticulo(Articulos articulo) {
    this.articulo = articulo;
  }

  public long getDiasRestantes() {
    return diasRestantes;
  }

  public void setDiasRestantes(long diasRestantes) {
    this.diasRestantes = diasRestantes;
  }

  public String getNivelUrgencia() {
    return nivelUrgencia;
  }

  public void setNivelUrgencia(String nivelUrgencia) {
    this.nivelUrgencia = nivelUrgencia;
  }
}
