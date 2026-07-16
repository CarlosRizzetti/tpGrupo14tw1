package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.ComandaSector;
import java.util.List;

public interface RepositorioComandaSector {
  ComandaSector buscarPorId(Long id);
  List<ComandaSector> listarVisiblesPorCategoria(Long idCategoria);

  void guardar(ComandaSector sector);

  void actualizar(ComandaSector sector);
}
