package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.RepositorioControlStock;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.services.ServicioEstadisticaImpl;
import com.tallerwebi.presentacion.dto.EstadisticasDTO;
import com.tallerwebi.presentacion.dto.PuntoEstadisticoDTO;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests de ServicioEstadisticaImpl. Usa un Clock fijo (lunes 2024-01-15, zona -03:00)
 * para que las agrupaciones por día, día de semana y hora sean deterministas.
 */
public class ServicioEstadisticaTest {

  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
  private static final ZoneOffset OFFSET = ZoneOffset.of("-03:00");

  private RepositorioTimer repositorioTimer;
  private RepositorioControlStock repositorioControlStock;
  private ServicioEstadisticaImpl servicio;

  @BeforeEach
  public void init() {
    repositorioTimer = mock(RepositorioTimer.class);
    repositorioControlStock = mock(RepositorioControlStock.class);
    // Lunes 2024-01-15 12:00 en zona -03:00
    Clock clock = Clock.fixed(Instant.parse("2024-01-15T15:00:00Z"), ZONA);
    servicio = new ServicioEstadisticaImpl(repositorioTimer, repositorioControlStock, clock);
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

  @Test
  @DisplayName("NEG-01 | obtenerEstadisticas | Lanza excepción si los días son cero o negativos")
  public void obtenerEstadisticasConDiasInvalidosDeberiaLanzarExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> servicio.obtenerEstadisticas(0));
    assertThrows(IllegalArgumentException.class, () -> servicio.obtenerEstadisticas(-5));
    verifyNoInteractions(repositorioTimer, repositorioControlStock);
  }

  @Test
  @DisplayName(
    "HP-01 | obtenerEstadisticas | Arma todas las series con los datos de los repositorios"
  )
  public void obtenerEstadisticasDeberiaArmarTodasLasSeries() {
    // Vencimientos: uno el martes 09/01 y otro el lunes 15/01 (dentro del rango)
    when(repositorioTimer.obtenerFechasCreacionDesde(any()))
      .thenReturn(Arrays.asList(fecha(2024, 1, 9, 9), fecha(2024, 1, 15, 9)));
    // Modificaciones de stock: una el miércoles 10/01
    when(repositorioControlStock.obtenerFechasMovimientosDesde(any()))
      .thenReturn(Collections.singletonList(fecha(2024, 1, 10, 8)));
    // Demanda (egresos): 2 el lunes a las 10hs y 1 el sábado a las 14hs
    when(repositorioControlStock.obtenerFechasEgresosDesde(any()))
      .thenReturn(
        Arrays.asList(fecha(2024, 1, 15, 10), fecha(2024, 1, 15, 10), fecha(2024, 1, 13, 14))
      );
    // Productos: incluye un nombre nulo para cubrir el "Sin nombre"
    when(repositorioTimer.contarVencimientosPorProducto(any()))
      .thenReturn(Arrays.asList(new Object[] { "Leche", 5L }, new Object[] { null, 2L }));
    // Estados: VENCIDO e IMPORTADO presentes, RENOVADO ausente (debe quedar en 0)
    when(repositorioTimer.contarPorEstado(any()))
      .thenReturn(
        Arrays.asList(
          new Object[] { EstadoTimer.VENCIDO, 3L },
          new Object[] { EstadoTimer.IMPORTADO, 1L }
        )
      );

    EstadisticasDTO dto = servicio.obtenerEstadisticas(7);

    // Vencimientos por día: 7 puntos, conteos en 09/01 y 15/01
    assertEquals(7, dto.getVencimientosPorDia().size());
    assertEquals(1, valorDe(dto.getVencimientosPorDia(), "09/01"));
    assertEquals(1, valorDe(dto.getVencimientosPorDia(), "15/01"));
    assertEquals(0, valorDe(dto.getVencimientosPorDia(), "12/01"));

    // Modificaciones por día: 7 puntos, conteo en 10/01
    assertEquals(7, dto.getModificacionesStockPorDia().size());
    assertEquals(1, valorDe(dto.getModificacionesStockPorDia(), "10/01"));

    // Demanda por día de semana: 7 puntos, Lunes=2, Sábado=1
    assertEquals(7, dto.getDemandaPorDiaSemana().size());
    assertEquals(2, valorDe(dto.getDemandaPorDiaSemana(), "Lunes"));
    assertEquals(1, valorDe(dto.getDemandaPorDiaSemana(), "Sábado"));
    assertEquals(0, valorDe(dto.getDemandaPorDiaSemana(), "Domingo"));

    // Demanda por hora: 24 puntos, 10:00=2, 14:00=1
    assertEquals(24, dto.getDemandaPorHora().size());
    assertEquals(2, valorDe(dto.getDemandaPorHora(), "10:00"));
    assertEquals(1, valorDe(dto.getDemandaPorHora(), "14:00"));
    assertEquals(0, valorDe(dto.getDemandaPorHora(), "00:00"));

    // Productos más utilizados: mapea nombre y "Sin nombre"
    assertEquals(2, dto.getProductosMasUtilizados().size());
    assertEquals(5, valorDe(dto.getProductosMasUtilizados(), "Leche"));
    assertEquals(2, valorDe(dto.getProductosMasUtilizados(), "Sin nombre"));

    // Vencimientos por estado: Vencidos=3, Importados=1, Renovados=0
    assertEquals(3, dto.getVencimientosPorEstado().size());
    assertEquals(3, valorDe(dto.getVencimientosPorEstado(), "Vencidos"));
    assertEquals(1, valorDe(dto.getVencimientosPorEstado(), "Importados"));
    assertEquals(0, valorDe(dto.getVencimientosPorEstado(), "Renovados"));
  }

  @Test
  @DisplayName("HP-02 | obtenerEstadisticas | Devuelve series vacías o en cero sin datos")
  public void obtenerEstadisticasSinDatosDeberiaDevolverSeriesEnCero() {
    when(repositorioTimer.obtenerFechasCreacionDesde(any())).thenReturn(Collections.emptyList());
    when(repositorioControlStock.obtenerFechasMovimientosDesde(any()))
      .thenReturn(Collections.emptyList());
    when(repositorioControlStock.obtenerFechasEgresosDesde(any()))
      .thenReturn(Collections.emptyList());
    when(repositorioTimer.contarVencimientosPorProducto(any())).thenReturn(Collections.emptyList());
    when(repositorioTimer.contarPorEstado(any())).thenReturn(Collections.emptyList());

    EstadisticasDTO dto = servicio.obtenerEstadisticas(1);

    assertEquals(1, dto.getVencimientosPorDia().size());
    assertEquals(0, valorDe(dto.getVencimientosPorDia(), "15/01"));
    assertEquals(24, dto.getDemandaPorHora().size());
    assertTrue(dto.getProductosMasUtilizados().isEmpty());
    // Todos los estados quedan en 0 al no haber filas
    assertEquals(0, valorDe(dto.getVencimientosPorEstado(), "Vencidos"));
    assertEquals(0, valorDe(dto.getVencimientosPorEstado(), "Renovados"));
  }
}
