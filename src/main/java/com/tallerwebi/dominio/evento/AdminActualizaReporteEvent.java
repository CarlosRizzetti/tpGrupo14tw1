package com.tallerwebi.dominio.evento;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminActualizaReporteEvent {

  private final Long reporteId;
  private final String nuevoEstado;

  public AdminActualizaReporteEvent(Long reporteId, String nuevoEstado) {
    this.reporteId = reporteId;
    this.nuevoEstado = nuevoEstado;
  }
}
