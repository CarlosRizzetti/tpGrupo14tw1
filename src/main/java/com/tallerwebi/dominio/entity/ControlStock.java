package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.TipoMovimientoStock;
import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ControlStock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "idProducto", nullable = false)
  private Producto producto;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "idTimer")
  private Timer timer;

  @Column(nullable = false)
  private Integer cantidad;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TipoMovimientoStock tipo;

  @Column(nullable = false)
  private OffsetDateTime fecha;
}
