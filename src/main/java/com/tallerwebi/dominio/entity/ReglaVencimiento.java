package com.tallerwebi.dominio.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "regla_vencimiento")
public class ReglaVencimiento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "producto_id")
  private Producto producto;

  private String ubicacion;
  private Integer duracionMinutos;
  private Boolean tieneDescongelamiento;
  private Integer descongelamientoMinutos;
}
