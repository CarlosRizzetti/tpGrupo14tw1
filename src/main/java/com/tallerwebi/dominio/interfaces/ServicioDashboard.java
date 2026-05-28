package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;

public interface ServicioDashboard {
  List<TimerDTO> obtenerTimersActivos(Long id);

  void eliminarTimer(Long timerId);

  CategoriaDto importarTimer(Long timerId, Long categoriaId);
}
