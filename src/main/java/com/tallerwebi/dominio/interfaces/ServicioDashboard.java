package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Timer;
import java.util.List;

public interface ServicioDashboard {
  List<Timer> obtenerTimersActivos(Long id);
}
