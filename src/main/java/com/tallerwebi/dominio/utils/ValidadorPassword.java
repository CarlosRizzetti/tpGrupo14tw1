package com.tallerwebi.dominio.utils;

public class ValidadorPassword {

  private ValidadorPassword() {
    // constructor privado para clase de utilidad
  }

  public static boolean esValida(String password) {
    if (password == null) return false;

    boolean tieneLargoValido = password.length() >= 6 && password.length() <= 10;
    boolean tieneMayuscula = password.chars().anyMatch(Character::isUpperCase);
    boolean tieneNumero = password.chars().anyMatch(Character::isDigit);

    return tieneLargoValido && tieneMayuscula && tieneNumero;
  }
}
