package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Categoria;
import java.util.List;

public interface RepositorioCategoria {
  List<Categoria> obtenerTodasLasCategoriasActivas();

  public void agregarNuevaCategoria(Categoria categoria);
}
