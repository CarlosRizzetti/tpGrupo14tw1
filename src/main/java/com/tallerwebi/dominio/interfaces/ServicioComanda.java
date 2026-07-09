package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;

public interface ServicioComanda {
  void sacarComanda(Long comandaId) throws IngredientesNoDisponiblesException;
}
