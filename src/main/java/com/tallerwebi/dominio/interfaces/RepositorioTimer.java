package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import java.time.OffsetDateTime;
import java.util.List;

public interface RepositorioTimer {
  List<Timer> obtenerTimersSegunEstado(Long id, EstadoTimer estado);
  List<Timer> obtenerTodosLosTimers();
  List<Timer> obtenerTimersConFiltro(EstadoTimer estado, Long categoriaId);
  Timer buscarPorId(Long id);
  void guardar(Timer timer);
  boolean existeTimerActivoEnCategoriaYGrupo(Long categoriaId, String groupId);

  /**
   * Fechas de creación de los vencimientos generados desde la fecha indicada.
   */
  List<OffsetDateTime> obtenerFechasCreacionDesde(OffsetDateTime desde);

  List<Timer> obtenerTimersActivosConStockPorProducto(Long productoId);
  /**
   * Conteo de vencimientos generados por producto desde la fecha indicada.
   *
   * @return filas {nombreProducto (String), cantidad (Long)} ordenadas de mayor a menor
   */
  List<Object[]> contarVencimientosPorProducto(OffsetDateTime desde);

  /**
   * Conteo de vencimientos por estado desde la fecha indicada.
   *
   * @return filas {estado (EstadoTimer), cantidad (Long)}
   */
  List<Object[]> contarPorEstado(OffsetDateTime desde);
}
