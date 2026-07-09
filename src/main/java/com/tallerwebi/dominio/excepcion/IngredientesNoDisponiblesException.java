package com.tallerwebi.dominio.excepcion;

import com.tallerwebi.dominio.entity.Producto;
import java.util.List;
import java.util.stream.Collectors;

public class IngredientesNoDisponiblesException extends Exception {

  private static final long serialVersionUID = 1L;
  private final List<Producto> faltantes;

  public IngredientesNoDisponiblesException(List<Producto> faltantes) {
    super(
      "Faltan timers activos con stock para: " +
      faltantes.stream().map(Producto::getNombre).collect(Collectors.joining(", "))
    );
    this.faltantes = faltantes;
  }

  public List<Producto> getFaltantes() {
    return faltantes;
  }
}
