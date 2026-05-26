package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;

public interface ServicioDashboard {
  List<TimerDTO> obtenerTimersActivos(Long id);

  void eliminarTimer(Long timerId);

  void importarTimer(Long timerId, Long categoriaId);
}
