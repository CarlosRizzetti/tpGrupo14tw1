package com.tallerwebi.dominio.entity;

import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class TrazabilidadDetalle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "idTrazabilidad")
  private Trazabilidad trazabilidad;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "idArticulo")
  private Articulos articulo;

  private Double cantidadUsada;
}
