package com.tallerwebi.dominio.interfaces;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Acceso a datos para las estadísticas. Devuelve las marcas de tiempo crudas;
 * la agregación (por día, día de semana y hora) la resuelve el servicio.
 */
public interface RepositorioEstadistica {
  List<OffsetDateTime> obtenerFechasCreacionVencimientos(OffsetDateTime desde);

  List<OffsetDateTime> obtenerFechasModificacionesStock(OffsetDateTime desde);

  List<OffsetDateTime> obtenerFechasDemanda(OffsetDateTime desde);

  /**
   * Conteo de vencimientos (Timer) generados por producto desde la fecha indicada.
   *
   * @return filas {nombreProducto (String), cantidad (Long)} ordenadas de mayor a menor
   */
  List<Object[]> obtenerConteoVencimientosPorProducto(OffsetDateTime desde);
}
