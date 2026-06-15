package com.tallerwebi.presentacion.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contenedor con las series estadísticas que se exponen al frontend.
 * Cada lista es una serie lista para graficar con Flowbite/ApexCharts.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {

  @Builder.Default
  private List<PuntoEstadisticoDTO> vencimientosPorDia = new ArrayList<>();

  @Builder.Default
  private List<PuntoEstadisticoDTO> modificacionesStockPorDia = new ArrayList<>();

  @Builder.Default
  private List<PuntoEstadisticoDTO> demandaPorDiaSemana = new ArrayList<>();

  @Builder.Default
  private List<PuntoEstadisticoDTO> demandaPorHora = new ArrayList<>();

  @Builder.Default
  private List<PuntoEstadisticoDTO> productosMasUtilizados = new ArrayList<>();
}
