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
public class ProductoFinalIngrediente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "productoFinalId")
  private ProductoFinal productoFinal;

  @ManyToOne
  @JoinColumn(name = "productoId")
  private Producto producto;

  private Integer cantidad;
}
