package com.tallerwebi.dominio.excepcion;

import org.springframework.security.core.AuthenticationException;

public class UsuarioSinCategorias extends AuthenticationException {

  private static final long serialVersionUID = 1L;

  public UsuarioSinCategorias(String message) {
    super(message);
  }
}
