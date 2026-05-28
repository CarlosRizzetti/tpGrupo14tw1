package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.repositorio.RepositorioTimerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioTimer")
@Transactional
public class ServicioTimer {

  private RepositorioTimer repositorioTimer;

  @Autowired
  public ServicioTimer(RepositorioTimerImpl repositorioTimer) {
    this.repositorioTimer = repositorioTimer;
  }

  public Timer buscarPorId(Long id) {
    return this.repositorioTimer.buscarPorId(id);
  }
}
