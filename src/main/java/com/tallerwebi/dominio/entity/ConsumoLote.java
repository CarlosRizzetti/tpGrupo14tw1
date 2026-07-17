package com.tallerwebi.dominio.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ConsumoLote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "idTimer")
  private Timer timer;

  @ManyToOne
  @JoinColumn(name = "idLote")
  private Lote lote;

  private Integer cantidadConsumida;
}
