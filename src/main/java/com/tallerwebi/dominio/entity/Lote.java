package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.EstadoLote;
import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Lote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "idProducto", nullable = false)
  private Producto producto;

  @Column(nullable = false)
  private OffsetDateTime fechaDeIngreso;

  @Column(nullable = false)
  private OffsetDateTime fechaDeVencimiento;

  private String proveedor;
  private String marca;
  private Long numeroDeLote;

  @Column(nullable = false)
  private Integer cantidadInicial;

  @Column(nullable = false)
  private Integer cantidadDisponible;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoLote estado;
}
