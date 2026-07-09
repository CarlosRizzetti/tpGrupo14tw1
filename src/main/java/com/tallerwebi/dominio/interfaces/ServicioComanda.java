package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.util.List;

public interface ServicioComanda {
  void sacarComanda(Long comandaId) throws IngredientesNoDisponiblesException;
  List<ComandaCocinaDTO> listarPendientesPorCategoria(Long idCategoria);

  int contarPendientesPorCategoria(Long idCategoria);
}
