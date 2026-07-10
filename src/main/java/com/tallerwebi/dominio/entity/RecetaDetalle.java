package com.tallerwebi.dominio.entity;

import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RecetaDetalle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "idReceta")
  private Receta receta;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "idArticulo")
  private Articulos articulo;

  private Double cantidad;
}
