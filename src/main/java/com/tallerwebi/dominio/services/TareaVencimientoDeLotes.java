package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.interfaces.ServicioLote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TareaVencimientoDeLotes {

  private final ServicioLote servicioLote;

  @Autowired
  public TareaVencimientoDeLotes(ServicioLote servicioLote) {
    this.servicioLote = servicioLote;
  }

  @Scheduled(cron = "0 0 * * * *")
  public void ejecutar() {
    servicioLote.marcarLotesVencidos();
  }
}
