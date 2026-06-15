package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.presentacion.dto.EstadisticasDTO;

/**
 * Lógica de negocio de las estadísticas del sistema.
 */
public interface ServicioEstadistica {
  /**
   * Devuelve las series estadísticas calculadas para los últimos {@code dias} días.
   *
   * @param dias cantidad de días hacia atrás a considerar (debe ser mayor a 0)
   * @return contenedor con las series listas para graficar
   */
  EstadisticasDTO obtenerEstadisticas(int dias);
}
