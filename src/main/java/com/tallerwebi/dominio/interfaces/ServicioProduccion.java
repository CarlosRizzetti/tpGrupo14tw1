package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Trazabilidad;
import java.util.List;

public interface ServicioProduccion {
  boolean procesarProduccion(Producto producto, Timer timer, Integer cantidadAProducir);
  boolean tieneReceta(Producto producto);

  List<com.tallerwebi.presentacion.dto.StockArticuloDto> obtenerStockAgrupado();

  List<Trazabilidad> obtenerTrazabilidadCompleta();
}
