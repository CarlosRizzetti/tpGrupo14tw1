package com.tallerwebi.dominio.entity;

import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Articulos {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private OffsetDateTime fechaDeIngreso;

  private String nombre;

  private String marca;

  private String proveedor;

  private Long numeroDeLote;

  @Column(nullable = false)
  private OffsetDateTime fechaDeVencimiento;
}
