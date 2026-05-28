package com.tallerwebi.dominio.excepcion;

public class IdInvalido extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  public IdInvalido(String message) {
    super(message);
  }
}
