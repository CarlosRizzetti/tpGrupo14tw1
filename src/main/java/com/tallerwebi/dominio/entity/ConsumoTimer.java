package com.tallerwebi.dominio.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsumoTimer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "detallePedidoIngredienteId")
  private DetallePedidoIngrediente detallePedidoIngrediente;

  @ManyToOne
  @JoinColumn(name = "timerId")
  private Timer timer;

  private Integer cantidadConsumida;
}
