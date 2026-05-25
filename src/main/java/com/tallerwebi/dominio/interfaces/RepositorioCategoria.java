package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Categoria;
import java.util.List;
import java.util.Set;

public interface RepositorioCategoria {
  Set<Categoria> obtenerTodasLasCategoriasActivas();
  void agregarNuevaCategoria(Categoria categoria);
  Categoria buscarPorId(Long id);
  Set<Categoria> obtenerCategoriasPorIds(List<Long> ids);
}
