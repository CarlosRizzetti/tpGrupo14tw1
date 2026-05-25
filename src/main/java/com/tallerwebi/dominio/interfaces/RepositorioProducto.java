package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Producto;
import java.util.List;

public interface RepositorioProducto {
  void guardar(Producto producto);
  Producto obtenerProductoPorId(Long id);
  List<Producto> obtenerProductosPorCategoria(Long categoriaId);
  Producto obtenerProductoConReglasYCategorias(Long id);
}
