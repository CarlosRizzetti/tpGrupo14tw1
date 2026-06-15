package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.ControlStock;
import java.time.OffsetDateTime;
import java.util.List;

public interface RepositorioControlStock {
  void guardar(ControlStock controlStock);
  List<ControlStock> obtenerPorProducto(Long productoId);

  /**
   * Fechas de todos los movimientos de stock (ingresos y egresos) desde la fecha indicada.
   */
  List<OffsetDateTime> obtenerFechasMovimientosDesde(OffsetDateTime desde);

  /**
   * Fechas de los movimientos de egreso (demanda) desde la fecha indicada.
   */
  List<OffsetDateTime> obtenerFechasEgresosDesde(OffsetDateTime desde);
}
