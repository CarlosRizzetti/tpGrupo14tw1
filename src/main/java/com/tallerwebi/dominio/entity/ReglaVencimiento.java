package com.tallerwebi.dominio.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ReglaVencimiento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "id_producto")
  private Producto producto;

  private String ubicacion;
  private Integer duracionMinutos;
  private Boolean tieneDescongelamiento;
  private Integer descongelamientoMinutos;
}
