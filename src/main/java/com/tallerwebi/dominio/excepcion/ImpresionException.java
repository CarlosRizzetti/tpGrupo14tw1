package com.tallerwebi.dominio.excepcion;

public class ImpresionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ImpresionException(String mensaje, Exception exception) {
    super(mensaje, exception);
  }

  public ImpresionException(String mensaje) {
    super(mensaje);
  }
}
