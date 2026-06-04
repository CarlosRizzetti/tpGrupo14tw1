package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.services.ServicioReglaVencimientoImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioReglaVencimientoTest {

  private ServicioReglaVencimientoImpl servicioReglaVencimiento;
  private RepositorioReglaVencimiento repositorioReglaVencimientoMock;
  private RepositorioTimer repositorioTimerMock;
  private Clock clock;

  @BeforeEach
  public void init() {
    clock = Clock.fixed(Instant.now(), ZoneOffset.ofHours(-3));
    repositorioReglaVencimientoMock = mock(RepositorioReglaVencimiento.class);
    repositorioTimerMock = mock(RepositorioTimer.class);
    servicioReglaVencimiento =
      new ServicioReglaVencimientoImpl(
        repositorioReglaVencimientoMock,
        repositorioTimerMock,
        clock
      );
  }

  // --- Validaciones de Regla de Vencimiento ---

  @Test
  public void guardarReglaVencimientoSinUbicacionDeberiaLanzarExcepcion() {
    // preparacion
    ReglaVencimiento regla = reglaValida();
    regla.setUbicacion(null);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioReglaVencimiento.guardarReglaVencimiento(regla)
    );
    assertThat(ex.getMessage(), equalToIgnoringCase("La ubicación es obligatoria"));
  }

  @Test
  public void guardarReglaVencimientoConDuracionCeroDeberiaLanzarExcepcion() {
    // preparacion
    ReglaVencimiento regla = reglaValida();
    regla.setDuracionMinutos(0);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioReglaVencimiento.guardarReglaVencimiento(regla)
    );
    assertThat(ex.getMessage(), equalToIgnoringCase("La duración debe ser mayor a 0"));
  }

  // --- Validaciones de Descongelamiento ---

  @Test
  public void guardarReglaVencimientoConDescongelamientoActivoSinMinutosDeberiaLanzarExcepcion() {
    // preparacion
    ReglaVencimiento regla = reglaValida();
    regla.setTieneDescongelamiento(true);
    regla.setDescongelamientoMinutos(null);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioReglaVencimiento.guardarReglaVencimiento(regla)
    );
    assertThat(
      ex.getMessage(),
      equalToIgnoringCase("Los minutos de descongelamiento son obligatorios")
    );
  }

  @Test
  public void guardarReglaVencimientoConDescongelamientoActivoConMinutosCeroDeberiaLanzarExcepcion() {
    // preparacion
    ReglaVencimiento regla = reglaValida();
    regla.setTieneDescongelamiento(true);
    regla.setDescongelamientoMinutos(0);

    // ejecucion y validacion
    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> servicioReglaVencimiento.guardarReglaVencimiento(regla)
    );
    assertThat(
      ex.getMessage(),
      equalToIgnoringCase("Los minutos de descongelamiento son obligatorios")
    );
  }

  // --- generarVencimiento ---

  @Test
  public void queSeGenereUnTimerCorrectamenteSegunLaReglaDeVencimiento() {
    // preparacion
    ReglaVencimiento regla = ReglaVencimiento
      .builder()
      .id(2L)
      .ubicacion("Heladera")
      .duracionMinutos(1080)
      .tieneDescongelamiento(true)
      .descongelamientoMinutos(120)
      .build();
    Categoria categoria = Categoria
      .builder()
      .id(1L)
      .icono("ejemplo")
      .estaActiva(true)
      .nombre("Cocina")
      .build();
    Producto producto = Producto.builder().id(1L).nombre("Dona").estaActivo(true).build();

    when(repositorioReglaVencimientoMock.obtenerReglaVencimientoPorId(2L)).thenReturn(regla);

    OffsetDateTime fechaCreacion = OffsetDateTime.now(clock);
    OffsetDateTime fechaVencimiento = fechaCreacion.plusMinutes(1080);
    OffsetDateTime descongelamiento = fechaCreacion.plusMinutes(120);
    Timer timerEsperado = new Timer(
      fechaCreacion,
      fechaVencimiento,
      descongelamiento,
      producto,
      categoria,
      regla
    );

    // ejecucion
    Timer timerGenerado = servicioReglaVencimiento.generarVencimiento(producto, categoria, 2L, 0);

    // validacion
    assertEquals(timerEsperado.getFechaVencimiento(), timerGenerado.getFechaVencimiento());
    assertEquals(timerEsperado.getFechaCreacion(), timerGenerado.getFechaCreacion());
    assertEquals(timerEsperado.getReglaVencimiento(), timerGenerado.getReglaVencimiento());
  }

  // --- Helper ---

  private ReglaVencimiento reglaValida() {
    ReglaVencimiento regla = new ReglaVencimiento();
    regla.setUbicacion("Freezer A");
    regla.setDuracionMinutos(60);
    regla.setTieneDescongelamiento(false);
    regla.setDescongelamientoMinutos(null);
    return regla;
  }
}
