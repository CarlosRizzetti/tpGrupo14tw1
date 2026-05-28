package com.tallerwebi.dominio.excepcion;

public class TimerNoEncontrado extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TimerNoEncontrado(String message) {
    super(message);
  }
}
