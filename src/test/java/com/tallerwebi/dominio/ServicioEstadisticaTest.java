package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.RepositorioLote;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.services.ServicioEstadisticaImpl;
import com.tallerwebi.presentacion.dto.EstadisticasDTO;
import com.tallerwebi.presentacion.dto.PuntoEstadisticoDTO;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServicioEstadisticaTest {

  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
  private static final ZoneOffset OFFSET = ZoneOffset.of("-03:00");

  private RepositorioTimer repositorioTimer;
  private ServicioEstadisticaImpl servicio;
  private RepositorioLote repositorioLoteMock;

  @BeforeEach
  public void init() {
    repositorioTimer = mock(RepositorioTimer.class);
    repositorioLoteMock = mock(RepositorioLote.class);

    Clock clock = Clock.fixed(Instant.parse("2024-01-15T15:00:00Z"), ZONA);
    servicio = new ServicioEstadisticaImpl(repositorioTimer, repositorioLoteMock, clock);
  }

  private OffsetDateTime fecha(int anio, int mes, int dia, int hora) {
    return OffsetDateTime.of(anio, mes, dia, hora, 0, 0, 0, OFFSET);
  }

  private long valorDe(List<PuntoEstadisticoDTO> serie, String etiqueta) {
    return serie
      .stream()
      .filter(p -> p.getEtiqueta().equals(etiqueta))
      .map(PuntoEstadisticoDTO::getValor)
      .findFirst()
      .orElseThrow(() -> new AssertionError("No existe la etiqueta: " + etiqueta));
  }

  /** Deja los 4 repos devolviendo listas vacías por defecto; cada test pisa lo que necesite. */
  private void stubRepositoriosVacios() {
    when(repositorioTimer.obtenerFechasCreacionDesde(any())).thenReturn(new ArrayList<>());
    when(repositorioLoteMock.obtenerFechasIngresoDesde(any())).thenReturn(new ArrayList<>());
    when(repositorioTimer.contarVencimientosPorProducto(any())).thenReturn(new ArrayList<>());
    when(repositorioTimer.contarPorEstado(any())).thenReturn(new ArrayList<>());
  }

  @Test
  @DisplayName("NEG-01 | obtenerEstadisticas | Lanza excepción si los días son cero o negativos")
  public void obtenerEstadisticasConDiasInvalidosDeberiaLanzarExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> servicio.obtenerEstadisticas(0));
    assertThrows(IllegalArgumentException.class, () -> servicio.obtenerEstadisticas(-5));
    verifyNoInteractions(repositorioTimer, repositorioLoteMock);
  }

  @Test
  @DisplayName(
    "HP-01 | obtenerEstadisticas | Calcula vencimientosPorDia agrupando las creaciones de Timer por día"
  )
  public void obtenerEstadisticasDeberiaCalcularVencimientosPorDia() {
    stubRepositoriosVacios();
    // hoy = 2024-01-15 (fijado por el clock). Con dias=3 el rango es 13,14,15 de enero.
    List<OffsetDateTime> fechasTimer = Arrays.asList(
      fecha(2024, 1, 13, 10),
      fecha(2024, 1, 13, 11),
      fecha(2024, 1, 14, 8)
    );
    when(repositorioTimer.obtenerFechasCreacionDesde(any())).thenReturn(fechasTimer);

    EstadisticasDTO resultado = servicio.obtenerEstadisticas(3);

    assertEquals(3, resultado.getVencimientosPorDia().size());
    assertEquals(2, valorDe(resultado.getVencimientosPorDia(), "13/01"));
    assertEquals(1, valorDe(resultado.getVencimientosPorDia(), "14/01"));
    assertEquals(0, valorDe(resultado.getVencimientosPorDia(), "15/01"));
  }

  @Test
  @DisplayName(
    "HP-02 | obtenerEstadisticas | Calcula modificacionesStockPorDia sumando ingresos de lote y creaciones de timer"
  )
  public void obtenerEstadisticasDeberiaCalcularModificacionesStockPorDia() {
    stubRepositoriosVacios();
    when(repositorioTimer.obtenerFechasCreacionDesde(any()))
      .thenReturn(Arrays.asList(fecha(2024, 1, 13, 10), fecha(2024, 1, 14, 8)));
    when(repositorioLoteMock.obtenerFechasIngresoDesde(any()))
      .thenReturn(Arrays.asList(fecha(2024, 1, 15, 9)));

    EstadisticasDTO resultado = servicio.obtenerEstadisticas(3);

    assertEquals(1, valorDe(resultado.getModificacionesStockPorDia(), "13/01"));
    assertEquals(1, valorDe(resultado.getModificacionesStockPorDia(), "14/01"));
    assertEquals(1, valorDe(resultado.getModificacionesStockPorDia(), "15/01")); // solo el ingreso de lote
  }

  @Test
  @DisplayName(
    "HP-03 | obtenerEstadisticas | Calcula demandaPorDiaSemana agrupando por día de la semana"
  )
  public void obtenerEstadisticasDeberiaCalcularDemandaPorDiaSemana() {
    stubRepositoriosVacios();
    // 13/01/2024 es sábado, 14/01/2024 es domingo, 15/01/2024 es lunes.
    when(repositorioTimer.obtenerFechasCreacionDesde(any()))
      .thenReturn(
        Arrays.asList(fecha(2024, 1, 13, 10), fecha(2024, 1, 13, 12), fecha(2024, 1, 14, 8))
      );

    EstadisticasDTO resultado = servicio.obtenerEstadisticas(3);

    assertEquals(7, resultado.getDemandaPorDiaSemana().size());
    assertEquals(2, valorDe(resultado.getDemandaPorDiaSemana(), "Sábado"));
    assertEquals(1, valorDe(resultado.getDemandaPorDiaSemana(), "Domingo"));
    assertEquals(0, valorDe(resultado.getDemandaPorDiaSemana(), "Lunes"));
    assertEquals(0, valorDe(resultado.getDemandaPorDiaSemana(), "Martes"));
  }

  @Test
  @DisplayName("HP-04 | obtenerEstadisticas | Calcula demandaPorHora agrupando por hora del día")
  public void obtenerEstadisticasDeberiaCalcularDemandaPorHora() {
    stubRepositoriosVacios();
    when(repositorioTimer.obtenerFechasCreacionDesde(any()))
      .thenReturn(
        Arrays.asList(fecha(2024, 1, 13, 10), fecha(2024, 1, 13, 10), fecha(2024, 1, 14, 8))
      );

    EstadisticasDTO resultado = servicio.obtenerEstadisticas(3);

    assertEquals(24, resultado.getDemandaPorHora().size());
    assertEquals(2, valorDe(resultado.getDemandaPorHora(), "10:00"));
    assertEquals(1, valorDe(resultado.getDemandaPorHora(), "08:00"));
    assertEquals(0, valorDe(resultado.getDemandaPorHora(), "00:00"));
  }

  @Test
  @DisplayName(
    "HP-05 | obtenerEstadisticas | Mapea productosMasUtilizados, usando 'Sin nombre' si el producto es null"
  )
  public void obtenerEstadisticasDeberiaMapearProductosMasUtilizados() {
    stubRepositoriosVacios();
    List<Object[]> conteoProductos = Arrays.asList(
      new Object[] { "Hamburguesa", 5L },
      new Object[] { null, 2L }
    );
    when(repositorioTimer.contarVencimientosPorProducto(any())).thenReturn(conteoProductos);

    EstadisticasDTO resultado = servicio.obtenerEstadisticas(3);

    assertEquals(2, resultado.getProductosMasUtilizados().size());
    assertEquals(5, valorDe(resultado.getProductosMasUtilizados(), "Hamburguesa"));
    assertEquals(2, valorDe(resultado.getProductosMasUtilizados(), "Sin nombre"));
  }

  @Test
  @DisplayName(
    "HP-06 | obtenerEstadisticas | Mapea vencimientosPorEstado completando con 0 los estados ausentes"
  )
  public void obtenerEstadisticasDeberiaMapearVencimientosPorEstadoConDefaultCero() {
    stubRepositoriosVacios();
    // IMPORTADO no está en la lista devuelta por el repo -> debe salir en 0.
    List<Object[]> conteoEstados = Arrays.asList(
      new Object[] { EstadoTimer.VENCIDO, 4L },
      new Object[] { EstadoTimer.RENOVADO, 2L }
    );
    when(repositorioTimer.contarPorEstado(any())).thenReturn(conteoEstados);

    EstadisticasDTO resultado = servicio.obtenerEstadisticas(3);

    assertEquals(3, resultado.getVencimientosPorEstado().size());
    assertEquals(4, valorDe(resultado.getVencimientosPorEstado(), "Vencidos"));
    assertEquals(0, valorDe(resultado.getVencimientosPorEstado(), "Importados"));
    assertEquals(2, valorDe(resultado.getVencimientosPorEstado(), "Renovados"));
  }

  @Test
  @DisplayName("EDGE-01 | obtenerEstadisticas | Con dias=1 el rango es un único día (hoy)")
  public void obtenerEstadisticasConUnSoloDiaDeberiaDevolverUnUnicoDia() {
    stubRepositoriosVacios();

    EstadisticasDTO resultado = servicio.obtenerEstadisticas(1);

    assertEquals(1, resultado.getVencimientosPorDia().size());
    assertEquals("15/01", resultado.getVencimientosPorDia().get(0).getEtiqueta());
    assertEquals(1, resultado.getModificacionesStockPorDia().size());
  }

  @Test
  @DisplayName(
    "EDGE-02 | obtenerEstadisticas | Sin datos en ningún repositorio, todas las series dan 0"
  )
  public void obtenerEstadisticasSinDatosDeberiaDevolverTodoEnCero() {
    stubRepositoriosVacios();

    EstadisticasDTO resultado = servicio.obtenerEstadisticas(5);

    assertEquals(5, resultado.getVencimientosPorDia().size());
    assertTrue(resultado.getVencimientosPorDia().stream().allMatch(p -> p.getValor() == 0));
    assertEquals(5, resultado.getModificacionesStockPorDia().size());
    assertTrue(resultado.getModificacionesStockPorDia().stream().allMatch(p -> p.getValor() == 0));
    assertEquals(7, resultado.getDemandaPorDiaSemana().size());
    assertTrue(resultado.getDemandaPorDiaSemana().stream().allMatch(p -> p.getValor() == 0));
    assertEquals(24, resultado.getDemandaPorHora().size());
    assertTrue(resultado.getDemandaPorHora().stream().allMatch(p -> p.getValor() == 0));
    assertTrue(resultado.getProductosMasUtilizados().isEmpty());
    assertEquals(3, resultado.getVencimientosPorEstado().size());
    assertTrue(resultado.getVencimientosPorEstado().stream().allMatch(p -> p.getValor() == 0));
  }
}
