package com.tallerwebi.dominio.excepcion;

public class ValidacionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ValidacionException(String message) {
    super(message);
  }
}
