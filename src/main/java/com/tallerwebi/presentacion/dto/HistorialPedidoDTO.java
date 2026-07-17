package com.tallerwebi.presentacion.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialPedidoDTO {

  private Long id;
  private Long clienteId;
  private String clienteNombre;
  private String estado;
  private String horaEntrada;
  private String horaSalida;
  private List<ItemPedidoDTO> items;
}
