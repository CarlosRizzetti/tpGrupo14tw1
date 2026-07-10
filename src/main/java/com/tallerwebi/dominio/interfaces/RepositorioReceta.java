package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Receta;
import java.util.List;

public interface RepositorioReceta {
  Receta buscarPorProducto(Producto producto);
  void guardar(Receta receta);
  List<Receta> obtenerTodas();
}
