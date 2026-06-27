package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Usuario;

public interface ServicioReglaVencimiento {
  void guardarReglaVencimiento(ReglaVencimiento reglaVencimiento);
  ReglaVencimiento obtenerReglaVencimientoPorId(Long id);
  Timer generarVencimiento(
    Producto producto,
    Categoria categoria,
    Long reglaId,
    Integer offsetMinutos,
    Integer cantidadUsada,
    Usuario usuario
  );
}
