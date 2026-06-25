package com.tallerwebi.dominio.excepcion;

import org.springframework.security.core.AuthenticationException;

public class UsuarioInactivo extends AuthenticationException {

  /* Identificador para la serialización de la clase, requerido por PMD en excepciones */
  private static final long serialVersionUID = 1L;

  public UsuarioInactivo(String message) {
    super(message);
  }
}
