package com.tallerwebi.dominio.excepcion;

public class CantidadInvalidaException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public CantidadInvalidaException(String message) {
    super(message);
  }
}
