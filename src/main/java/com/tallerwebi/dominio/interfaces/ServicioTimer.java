package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;

public interface ServicioTimer {
  List<TimerDTO> obtenerTimersActivos(Long categoriaId);
  void modificarEstado(Long timerId, EstadoTimer estado);
  CategoriaDto importarTimer(Long timerId, Long categoriaId);
  Timer buscarPorId(Long id);
  TimerDTO renovarTimer(Timer timer);
}
