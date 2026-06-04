package com.tallerwebi.dominio.interfaces;

public interface ServicioValidacionIdentidad {
  void solicitarValidacion(String email);
  boolean validarToken(String token);
}
