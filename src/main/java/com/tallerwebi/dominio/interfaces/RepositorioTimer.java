package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import java.util.List;

public interface RepositorioTimer {
  List<Timer> obtenerTimersSegunEstado(Long id, EstadoTimer estado);
  List<Timer> obtenerTodosLosTimers();
  List<Timer> obtenerTimersConFiltro(EstadoTimer estado, Long categoriaId);
  Timer buscarPorId(Long id);
  void guardar(Timer timer);
  boolean existeTimerActivoEnCategoriaYGrupo(Long categoriaId, String groupId);
}
