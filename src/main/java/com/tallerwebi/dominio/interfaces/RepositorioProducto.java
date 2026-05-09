package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Categoria;
import com.tallerwebi.dominio.Producto;
import java.util.List;

public interface RepositorioProducto {
  void guardar(Producto producto);
  List<Categoria> obtenerCategoriasPorIds(List<Long> ids);
}
