package com.tallerwebi.presentacion.dto;

import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimerDTO {

  private Long id;
  private EstadoTimer estado;
  private String nombre;
  private String groupId;
  private CicloVidaDTO cicloVida;
  private String ubicacion;
  private Integer cantidad;
  private String usuario;
  private CategoriaDto categoria;
  private List<LoteConsumidoDTO> lotesUtilizados;
}
