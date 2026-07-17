package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;

public interface ServicioTimer {
  List<TimerDTO> obtenerTimersActivos(Long categoriaId);
  void modificarEstado(Long timerId, EstadoTimer estado);
  CategoriaDto importarTimer(Long timerId, Long categoriaId, Integer cantidad, Usuario usuario);
  Timer buscarPorId(Long id);
  TimerDTO renovarTimer(Timer timer, Integer cantidad, Usuario usuario);
  List<TimerDTO> obtenerTodosLosTimers();
  List<TimerDTO> obtenerTimersConFiltro(EstadoTimer estado, Long categoriaId);
  void descontarStock(Long timerId, Integer cantidad);
  List<Timer> obtenerTimersActivosConStockPorProducto(Long idProducto);
  TimerDTO buscarPorIdDTO(Long id);
}
