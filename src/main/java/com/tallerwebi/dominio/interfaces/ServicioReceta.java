package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Receta;
import java.util.List;

public interface ServicioReceta {
  void guardarReceta(Producto producto, List<Long> articulosIds, List<Double> cantidades);
  Receta buscarPorProducto(Producto producto);
  List<Receta> obtenerTodas();
}
