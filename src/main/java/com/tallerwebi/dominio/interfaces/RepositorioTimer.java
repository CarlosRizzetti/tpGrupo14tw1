package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Timer;
import java.util.List;

public interface RepositorioTimer {
  List<Timer> obtenerTimersSegunEstado(Long id, String estado);
}
