package com.tallerwebi.presentacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un punto de una serie estadística: una etiqueta (eje X) y su valor (eje Y).
 * Se usa para alimentar los gráficos de Flowbite/ApexCharts.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PuntoEstadisticoDTO {

  private String etiqueta;
  private Long valor;
}
