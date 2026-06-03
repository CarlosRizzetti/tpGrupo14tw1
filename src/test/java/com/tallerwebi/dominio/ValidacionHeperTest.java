package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.excepcion.IdInvalido;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import javax.xml.bind.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ValidacionHeperTest {

  // ---- ID validations ----

  @Test
  void shouldPassWithValidId() {
    assertDoesNotThrow(() -> ValidacionHelper.validarId(1L));
  }

  @Test
  void shouldThrowWhenIdIsNull() {
    IdInvalido ex = assertThrows(IdInvalido.class, () -> ValidacionHelper.validarId(null));
    assertTrue(ex.getMessage().contains("El id no puede ser nulo"));
  }

  @Test
  void shouldThrowWhenIdIsZero() {
    IdInvalido ex = assertThrows(IdInvalido.class, () -> ValidacionHelper.validarId(0L));
    assertTrue(ex.getMessage().contains("positivo"));
  }

  @Test
  void shouldThrowWhenIdIsNegative() {
    IdInvalido ex = assertThrows(IdInvalido.class, () -> ValidacionHelper.validarId(-1L));
    assertTrue(ex.getMessage().contains("positivo"));
  }

  // ---- Field length ----

  @Test
  void shouldPassWhenFieldIsAtMaxLength() {
    assertDoesNotThrow(() -> ValidacionHelper.validarLongitudDeCampo("A".repeat(500), "field"));
  }

  @Test
  void shouldThrowWhenFieldExceedsMaxLength() {
    ValidacionException ex = assertThrows(
      ValidacionException.class,
      () -> ValidacionHelper.validarLongitudDeCampo("A".repeat(501), "field")
    );
    assertTrue(ex.getMessage().contains("'field' excede la longitud máxima"));
  }

  @Test
  void shouldPassWhenFieldIsNull() {
    assertDoesNotThrow(() -> ValidacionHelper.validarLongitudDeCampo(null, "field"));
  }

  // ---- Safe characters ----

  @ParameterizedTest
  @ValueSource(
    strings = {
      "<script>alert('xss')</script>",
      "' OR '1'='1'; DROP TABLE users;--",
      "<b>bold</b>",
      "zona; rm -rf /",
      "test()",
      "value&other",
    }
  )
  void shouldThrowWhenDangerousCharactersArePresent(String dangerousValue) {
    ValidacionException ex = assertThrows(
      ValidacionException.class,
      () -> ValidacionHelper.validateCaracteresSeguros(dangerousValue, "field")
    );
    assertTrue(ex.getMessage().contains("contiene caracteres inválidos"));
  }

  @ParameterizedTest
  @ValueSource(
    strings = { "Normal Name", "Almacén Central", "Zone-Norte_1", "valid.email@domain.com" }
  )
  void shouldPassWhenValueIsSafe(String safeValue) {
    assertDoesNotThrow(() -> ValidacionHelper.validateCaracteresSeguros(safeValue, "field"));
  }

  // ---- requireNonNull ----

  @Test
  void shouldReturnValueWhenNotNull() {
    Timer timer = new Timer();
    assertEquals(timer, ValidacionHelper.queNoSeaNull(timer, "timer"));
  }

  @Test
  void shouldThrowWhenValueIsNull() {
    ValidacionException ex = assertThrows(
      ValidacionException.class,
      () -> ValidacionHelper.queNoSeaNull(null, "timer")
    );
    assertTrue(ex.getMessage().contains("'timer' no puede ser nulo"));
  }
}
