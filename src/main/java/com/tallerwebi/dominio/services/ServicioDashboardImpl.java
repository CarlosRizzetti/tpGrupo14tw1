package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioDashboard")
@Transactional
public class ServicioDashboardImpl implements ServicioDashboard {

  public RepositorioTimer repositorioTimer;

  @Autowired
  public ServicioDashboardImpl(RepositorioTimer repositorioTimer) {
    this.repositorioTimer = repositorioTimer;
  }

  @Override
  public List<Timer> obtenerTimersActivos(Long id) {
    return this.repositorioTimer.obtenerTimersSegunEstado(id, "activo");
  }
}
