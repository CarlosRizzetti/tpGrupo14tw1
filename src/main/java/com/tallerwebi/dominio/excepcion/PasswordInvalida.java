package com.tallerwebi.dominio.excepcion;

public class PasswordInvalida extends Exception {

  private static final long serialVersionUID = 1L;

  public PasswordInvalida(String message) {
    super(message);
  }
}
