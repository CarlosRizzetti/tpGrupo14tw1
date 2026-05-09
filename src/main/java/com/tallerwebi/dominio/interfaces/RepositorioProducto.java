package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import java.util.List;

public interface RepositorioProducto {
  void guardar(Producto producto);
  List<Categoria> obtenerCategoriasPorIds(List<Long> ids);
}
