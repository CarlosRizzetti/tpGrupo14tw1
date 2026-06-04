package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.ControlStock;
import java.util.List;

public interface RepositorioControlStock {
  void guardar(ControlStock controlStock);
  List<ControlStock> obtenerPorProducto(Long productoId);
}
