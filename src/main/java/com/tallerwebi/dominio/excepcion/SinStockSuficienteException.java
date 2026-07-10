package com.tallerwebi.dominio.excepcion;

public class SinStockSuficienteException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SinStockSuficienteException(String message) {
    super(message);
  }
}
