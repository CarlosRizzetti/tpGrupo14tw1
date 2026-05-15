package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Categoria;
import java.util.List;

public interface RepositorioCategoria {
  List<Categoria> obtenerTodasLasCategoriasActivas();
  void agregarNuevaCategoria(Categoria categoria);
  Categoria buscarPorId(Long id);
}
