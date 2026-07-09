package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.ProductoFinal;
import java.util.List;

public interface RepositorioProductoFinal {
  ProductoFinal buscarPorId(Long id);

  List<ProductoFinal> listarTodos();

  List<ProductoFinal> listarPorCategoria(Long idCategoria);
}
