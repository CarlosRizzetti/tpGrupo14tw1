package com.tallerwebi.dominio.utils;

import com.tallerwebi.dominio.excepcion.IdInvalido;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class ValidacionHelper {

  private static final int LONGITUD_MAXIMA_DE_CAMPO = 500;
  private static final String CARACTERES_PELIGROSOS_REGEX = ".*[<>\"'%;()&+].*";

  private ValidacionHelper() {
    throw new UnsupportedOperationException("Clase de utlilidades");
  }

  // ---- ID validations ----

  public static void validarId(Long id) {
    if (id == null) {
      throw new IdInvalido("El id no puede ser nulo");
    }
    if (id <= 0) {
      throw new IdInvalido("El ID tiene que ser un número positivo, ID recibido: " + id);
    }
  }

  // ---- String validations ----

  public static void validarLongitudDeCampo(String valor, String nombreDelCampo) {
    if (valor != null && valor.length() > LONGITUD_MAXIMA_DE_CAMPO) {
      throw new ValidacionException(
        "El campo '" + nombreDelCampo + "' excede la longitud máxima de " + LONGITUD_MAXIMA_DE_CAMPO
      );
    }
  }

  public static void validateCaracteresSeguros(String valor, String nombreDelCampo) {
    if (valor != null && valor.matches(CARACTERES_PELIGROSOS_REGEX)) {
      throw new ValidacionException(
        "El campo '" + nombreDelCampo + "' contiene caracteres inválidos"
      );
    }
  }

  public static void validarCampoSeguro(String valor, String nombreDelCampo) {
    validarLongitudDeCampo(valor, nombreDelCampo);
    validateCaracteresSeguros(valor, nombreDelCampo);
  }

  // ---- Null validations ----

  public static <T> T queNoSeaNull(T valor, String nombreDelCampo) {
    if (valor == null) {
      throw new ValidacionException("El campo '" + nombreDelCampo + "' no puede ser nulo");
    }
    return valor;
  }

  public static <T> List<T> queLaListaNoSeaNull(List<T> lista, String contexto) {
    if (lista == null) {
      throw new ValidacionException("La lista de '" + contexto + "' retornó null");
    }
    return lista;
  }

  public static <T> Set<T> queElSetNoSeaNull(Set<T> set, String contexto) {
    if (set == null || set.isEmpty()) {
      throw new ValidacionException("El set de '" + contexto + "' retornó null o está vacío");
    }
    return set;
  }

  // ---- Date validations ----

  public static void validarRangoDeFecha(LocalDateTime desde, LocalDateTime hasta) {
    if (desde != null && hasta != null && desde.isAfter(hasta)) {
      throw new ValidacionException("La fecha de inicio no puede ser posterior a la fecha final");
    }
  }
}
