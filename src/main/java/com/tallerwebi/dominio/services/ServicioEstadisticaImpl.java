package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.interfaces.RepositorioEstadistica;
import com.tallerwebi.dominio.interfaces.ServicioEstadistica;
import com.tallerwebi.presentacion.dto.EstadisticasDTO;
import com.tallerwebi.presentacion.dto.PuntoEstadisticoDTO;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de estadísticas. Obtiene las marcas de tiempo del
 * repositorio y las agrupa por día, día de semana y hora para graficarlas.
 */
@Service("servicioEstadistica")
@Transactional
public class ServicioEstadisticaImpl implements ServicioEstadistica {

  private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM");

  private static final String[] NOMBRES_DIA_SEMANA = {
    "Lunes",
    "Martes",
    "Miércoles",
    "Jueves",
    "Viernes",
    "Sábado",
    "Domingo",
  };

  private final RepositorioEstadistica repositorioEstadistica;
  private final Clock clock;

  @Autowired
  public ServicioEstadisticaImpl(RepositorioEstadistica repositorioEstadistica, Clock clock) {
    this.repositorioEstadistica = repositorioEstadistica;
    this.clock = clock;
  }

  @Override
  public EstadisticasDTO obtenerEstadisticas(int dias) {
    if (dias <= 0) {
      throw new IllegalArgumentException("La cantidad de días debe ser mayor a cero");
    }

    ZoneId zona = clock.getZone();
    LocalDate hoy = LocalDate.now(clock);
    LocalDate desdeDia = hoy.minusDays((long) dias - 1);
    OffsetDateTime desde = desdeDia.atStartOfDay(zona).toOffsetDateTime();

    List<OffsetDateTime> fechasVencimientos =
      repositorioEstadistica.obtenerFechasCreacionVencimientos(desde);
    List<OffsetDateTime> fechasModificaciones =
      repositorioEstadistica.obtenerFechasModificacionesStock(desde);
    List<OffsetDateTime> fechasDemanda = repositorioEstadistica.obtenerFechasDemanda(desde);
    List<Object[]> conteoProductos = repositorioEstadistica.obtenerConteoVencimientosPorProducto(
      desde
    );

    return EstadisticasDTO
      .builder()
      .vencimientosPorDia(agruparPorDia(fechasVencimientos, desdeDia, hoy, zona))
      .modificacionesStockPorDia(agruparPorDia(fechasModificaciones, desdeDia, hoy, zona))
      .demandaPorDiaSemana(agruparPorDiaSemana(fechasDemanda, zona))
      .demandaPorHora(agruparPorHora(fechasDemanda, zona))
      .productosMasUtilizados(mapearConteoProductos(conteoProductos))
      .build();
  }

  private List<PuntoEstadisticoDTO> mapearConteoProductos(List<Object[]> filas) {
    return filas
      .stream()
      .map(fila ->
        new PuntoEstadisticoDTO(
          Objects.toString(fila[0], "Sin nombre"),
          ((Number) fila[1]).longValue()
        )
      )
      .collect(Collectors.toList());
  }

  private List<PuntoEstadisticoDTO> agruparPorDia(
    List<OffsetDateTime> fechas,
    LocalDate desde,
    LocalDate hasta,
    ZoneId zona
  ) {
    return Stream
      .iterate(desde, fecha -> fecha.plusDays(1))
      .limit(ChronoUnit.DAYS.between(desde, hasta) + 1L)
      .map(dia -> new PuntoEstadisticoDTO(dia.format(FORMATO_DIA), contarPorDia(fechas, zona, dia)))
      .collect(Collectors.toList());
  }

  private List<PuntoEstadisticoDTO> agruparPorDiaSemana(List<OffsetDateTime> fechas, ZoneId zona) {
    return Stream
      .of(DayOfWeek.values())
      .map(dia ->
        new PuntoEstadisticoDTO(
          NOMBRES_DIA_SEMANA[dia.getValue() - 1],
          contarPorDiaSemana(fechas, zona, dia)
        )
      )
      .collect(Collectors.toList());
  }

  private List<PuntoEstadisticoDTO> agruparPorHora(List<OffsetDateTime> fechas, ZoneId zona) {
    return IntStream
      .range(0, 24)
      .mapToObj(hora ->
        new PuntoEstadisticoDTO(String.format("%02d:00", hora), contarPorHora(fechas, zona, hora))
      )
      .collect(Collectors.toList());
  }

  private long contarPorDia(List<OffsetDateTime> fechas, ZoneId zona, LocalDate dia) {
    return fechas
      .stream()
      .filter(fecha -> fecha.atZoneSameInstant(zona).toLocalDate().equals(dia))
      .count();
  }

  private long contarPorDiaSemana(List<OffsetDateTime> fechas, ZoneId zona, DayOfWeek dia) {
    return fechas
      .stream()
      .filter(fecha -> fecha.atZoneSameInstant(zona).getDayOfWeek() == dia)
      .count();
  }

  private long contarPorHora(List<OffsetDateTime> fechas, ZoneId zona, int hora) {
    return fechas.stream().filter(fecha -> fecha.atZoneSameInstant(zona).getHour() == hora).count();
  }
}
