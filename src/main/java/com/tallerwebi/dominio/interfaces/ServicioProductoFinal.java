package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.ProductoFinal;
import java.util.List;

public interface ServicioProductoFinal {
  List<ProductoFinal> listarTodos();

  ProductoFinal buscarPorId(Long id);

  List<ProductoFinal> listarPorCategoria(Long idCategoria);

  void guardarProductoFinal(
    ProductoFinal productoFinal,
    Long idCategoria,
    List<Long> idIngredientes,
    List<Integer> cantidades
  );
}
