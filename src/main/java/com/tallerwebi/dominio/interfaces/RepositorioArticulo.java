package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Articulos;
import java.util.List;

public interface RepositorioArticulo {
  List<Articulos> obtenerTodos();
  void guardar(Articulos articulo);
  Articulos buscarPorId(Long id);
  List<Articulos> buscarPorNombre(String nombre);
  List<com.tallerwebi.presentacion.dto.StockArticuloDto> obtenerStockAgrupadoPorNombre();
}
