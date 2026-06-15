package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.RepositorioEstadistica;
import com.tallerwebi.dominio.services.ServicioEstadisticaImpl;
import com.tallerwebi.presentacion.dto.EstadisticasDTO;
import com.tallerwebi.presentacion.dto.PuntoEstadisticoDTO;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServicioEstadisticaTest {

  private static final ZoneId ZONA = ZoneOffset.ofHours(-3);

  private ServicioEstadisticaImpl servicioEstadistica;
  private RepositorioEstadistica repositorioEstadisticaMock;
  private Clock clock;

  @BeforeEach
  public void init() {
    // Miércoles 2026-06-10 14:00 (-03:00)
    Instant ahora = OffsetDateTime.of(2026, 6, 10, 14, 0, 0, 0, ZoneOffset.ofHours(-3)).toInstant();
    clock = Clock.fixed(ahora, ZONA);
    repositorioEstadisticaMock = mock(RepositorioEstadistica.class);
    servicioEstadistica = new ServicioEstadisticaImpl(repositorioEstadisticaMock, clock);
  }

  private OffsetDateTime fecha(int anio, int mes, int dia, int hora) {
    return OffsetDateTime.of(anio, mes, dia, hora, 0, 0, 0, ZoneOffset.ofHours(-3));
  }

  private long valorDeEtiqueta(List<PuntoEstadisticoDTO> serie, String etiqueta) {
    return serie
      .stream()
      .filter(p -> p.getEtiqueta().equals(etiqueta))
      .map(PuntoEstadisticoDTO::getValor)
      .findFirst()
      .orElse(-1L);
  }

  @Test
  @DisplayName("NEG-01 | obtenerEstadisticas | Lanza excepción si los días son cero")
  public void obtenerEstadisticasConCeroDiasDeberiaLanzarExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> servicioEstadistica.obtenerEstadisticas(0));
  }

  @Test
  @DisplayName("NEG-02 | obtenerEstadisticas | Lanza excepción si los días son negativos")
  public void obtenerEstadisticasConDiasNegativosDeberiaLanzarExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> servicioEstadistica.obtenerEstadisticas(-5));
  }

  @Test
  @DisplayName(
    "HAP-01 | obtenerEstadisticas | Genera una serie con un punto por cada día del rango"
  )
  public void obtenerEstadisticasDeberiaGenerarUnPuntoPorDia() {
    when(repositorioEstadisticaMock.obtenerFechasCreacionVencimientos(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasModificacionesStock(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasDemanda(any())).thenReturn(List.of());

    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(7);

    assertEquals(7, resultado.getVencimientosPorDia().size());
    assertEquals(7, resultado.getModificacionesStockPorDia().size());
  }

  @Test
  @DisplayName("HAP-02 | obtenerEstadisticas | Cuenta los vencimientos creados en el día correcto")
  public void obtenerEstadisticasDeberiaContarVencimientosPorDia() {
    when(repositorioEstadisticaMock.obtenerFechasCreacionVencimientos(any()))
      .thenReturn(List.of(fecha(2026, 6, 10, 9), fecha(2026, 6, 10, 18), fecha(2026, 6, 9, 11)));
    when(repositorioEstadisticaMock.obtenerFechasModificacionesStock(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasDemanda(any())).thenReturn(List.of());

    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(7);

    assertEquals(2L, valorDeEtiqueta(resultado.getVencimientosPorDia(), "10/06"));
    assertEquals(1L, valorDeEtiqueta(resultado.getVencimientosPorDia(), "09/06"));
  }

  @Test
  @DisplayName("HAP-03 | obtenerEstadisticas | Cuenta las modificaciones de stock por día")
  public void obtenerEstadisticasDeberiaContarModificacionesStockPorDia() {
    when(repositorioEstadisticaMock.obtenerFechasCreacionVencimientos(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasModificacionesStock(any()))
      .thenReturn(List.of(fecha(2026, 6, 8, 10), fecha(2026, 6, 8, 12), fecha(2026, 6, 8, 20)));
    when(repositorioEstadisticaMock.obtenerFechasDemanda(any())).thenReturn(List.of());

    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(7);

    assertEquals(3L, valorDeEtiqueta(resultado.getModificacionesStockPorDia(), "08/06"));
  }

  @Test
  @DisplayName("HAP-04 | obtenerEstadisticas | Agrupa la demanda por día de la semana")
  public void obtenerEstadisticasDeberiaAgruparDemandaPorDiaSemana() {
    // 2026-06-08 es lunes, 2026-06-10 es miércoles
    when(repositorioEstadisticaMock.obtenerFechasCreacionVencimientos(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasModificacionesStock(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasDemanda(any()))
      .thenReturn(List.of(fecha(2026, 6, 8, 10), fecha(2026, 6, 8, 16), fecha(2026, 6, 10, 9)));

    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(7);

    assertEquals(7, resultado.getDemandaPorDiaSemana().size());
    assertEquals(2L, valorDeEtiqueta(resultado.getDemandaPorDiaSemana(), "Lunes"));
    assertEquals(1L, valorDeEtiqueta(resultado.getDemandaPorDiaSemana(), "Miércoles"));
    assertEquals(0L, valorDeEtiqueta(resultado.getDemandaPorDiaSemana(), "Domingo"));
  }

  @Test
  @DisplayName("HAP-05 | obtenerEstadisticas | Agrupa la demanda por hora del día (24 puntos)")
  public void obtenerEstadisticasDeberiaAgruparDemandaPorHora() {
    when(repositorioEstadisticaMock.obtenerFechasCreacionVencimientos(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasModificacionesStock(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasDemanda(any()))
      .thenReturn(List.of(fecha(2026, 6, 10, 9), fecha(2026, 6, 9, 9), fecha(2026, 6, 8, 20)));

    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(7);

    assertEquals(24, resultado.getDemandaPorHora().size());
    assertEquals(2L, valorDeEtiqueta(resultado.getDemandaPorHora(), "09:00"));
    assertEquals(1L, valorDeEtiqueta(resultado.getDemandaPorHora(), "20:00"));
    assertEquals(0L, valorDeEtiqueta(resultado.getDemandaPorHora(), "00:00"));
  }

  @Test
  @DisplayName("HAP-06 | obtenerEstadisticas | Ignora fechas fuera del rango solicitado")
  public void obtenerEstadisticasDeberiaIgnorarFechasFueraDeRango() {
    when(repositorioEstadisticaMock.obtenerFechasCreacionVencimientos(any()))
      .thenReturn(List.of(fecha(2026, 6, 10, 9), fecha(2026, 1, 1, 9)));
    when(repositorioEstadisticaMock.obtenerFechasModificacionesStock(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasDemanda(any())).thenReturn(List.of());

    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(7);

    long total = resultado
      .getVencimientosPorDia()
      .stream()
      .mapToLong(PuntoEstadisticoDTO::getValor)
      .sum();
    assertEquals(1L, total);
  }

  @Test
  @DisplayName("HAP-07 | obtenerEstadisticas | Pide al repositorio la fecha de inicio del rango")
  public void obtenerEstadisticasDeberiaConsultarRepositorioConFechaDesde() {
    when(repositorioEstadisticaMock.obtenerFechasCreacionVencimientos(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasModificacionesStock(any())).thenReturn(List.of());
    when(repositorioEstadisticaMock.obtenerFechasDemanda(any())).thenReturn(List.of());

    servicioEstadistica.obtenerEstadisticas(7);

    verify(repositorioEstadisticaMock, times(1)).obtenerFechasCreacionVencimientos(any());
    verify(repositorioEstadisticaMock, times(1)).obtenerFechasModificacionesStock(any());
    verify(repositorioEstadisticaMock, times(1)).obtenerFechasDemanda(any());
  }

  @Test
  @DisplayName(
    "HAP-08 | obtenerEstadisticas | Mapea el conteo de vencimientos por producto a la serie"
  )
  public void obtenerEstadisticasDeberiaMapearProductosMasUtilizados() {
    when(repositorioEstadisticaMock.obtenerConteoVencimientosPorProducto(any()))
      .thenReturn(List.of(new Object[] { "Hamburguesa", 5L }, new Object[] { "Café", 2L }));

    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(30);

    assertEquals(2, resultado.getProductosMasUtilizados().size());
    assertEquals("Hamburguesa", resultado.getProductosMasUtilizados().get(0).getEtiqueta());
    assertEquals(5L, resultado.getProductosMasUtilizados().get(0).getValor());
    assertEquals("Café", resultado.getProductosMasUtilizados().get(1).getEtiqueta());
    assertEquals(2L, resultado.getProductosMasUtilizados().get(1).getValor());
  }

  @Test
  @DisplayName(
    "HAP-09 | obtenerEstadisticas | Soporta el conteo como cualquier tipo numérico (BigInteger)"
  )
  public void obtenerEstadisticasDeberiaSoportarConteoBigInteger() {
    when(repositorioEstadisticaMock.obtenerConteoVencimientosPorProducto(any()))
      .thenReturn(
        java.util.Collections.singletonList(
          new Object[] { "Papas", java.math.BigInteger.valueOf(3) }
        )
      );

    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(30);

    assertEquals(1, resultado.getProductosMasUtilizados().size());
    assertEquals(3L, resultado.getProductosMasUtilizados().get(0).getValor());
  }

  @Test
  @DisplayName("NEG-03 | obtenerEstadisticas | Serie de productos vacía cuando no hay vencimientos")
  public void obtenerEstadisticasDeberiaDevolverProductosVaciosSinDatos() {
    EstadisticasDTO resultado = servicioEstadistica.obtenerEstadisticas(30);

    assertTrue(resultado.getProductosMasUtilizados().isEmpty());
  }
}
