package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;

public interface ServicioControlStock {
  void registrarMovimiento(
    Producto producto,
    Timer timer,
    Integer cantidad,
    TipoMovimientoStock tipo
  );
}
