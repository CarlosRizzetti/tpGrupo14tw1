package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.TipoArticulo;
import com.tallerwebi.dominio.entity.enums.UnidadDeMedida;
import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@SuppressWarnings("PMD.TooManyFields")
public class Articulos {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private OffsetDateTime fechaDeIngreso;

  private Long codigo;

  private String nombre;

  private String marca;

  private String proveedor;

  private Long numeroDeLote;

  @Column(nullable = false)
  private OffsetDateTime fechaDeVencimiento;

  private Double cantidad;

  @Enumerated(EnumType.STRING)
  private UnidadDeMedida unidadDeMedida;

  @Enumerated(EnumType.STRING)
  private TipoArticulo tipoArticulo;
}
