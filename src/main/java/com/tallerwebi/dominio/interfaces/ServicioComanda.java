package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.util.List;

public interface ServicioComanda {
  void crearSectoresDeComanda(Comanda comanda);

  void servirSector(Long idSector) throws IngredientesNoDisponiblesException;

  List<ComandaCocinaDTO> listarPendientesPorCategoria(Long idCategoria);

  int contarPendientesPorCategoria(Long idCategoria);
}
