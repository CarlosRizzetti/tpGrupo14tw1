package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.ReglaVencimiento;

public interface RepositorioReglaVencimiento {
  void guardar(ReglaVencimiento reglaVencimiento);
  ReglaVencimiento obtenerReglaVencimientoPorId(Long id);
}
