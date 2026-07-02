package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Trazabilidad;
import java.util.List;

public interface RepositorioTrazabilidad {
  void guardar(Trazabilidad trazabilidad);
  List<Trazabilidad> obtenerTodas();
}
