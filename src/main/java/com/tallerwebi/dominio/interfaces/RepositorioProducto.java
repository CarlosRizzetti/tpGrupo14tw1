package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import java.util.List;

public interface RepositorioProducto {
  void guardar(Producto producto);
  Producto obtenerProductoPorId(Long id);
  List<Categoria> obtenerCategoriasPorIds(List<Long> ids);
  List<Producto> obtenerProductosPorCategoria(Long categoriaId);
}
