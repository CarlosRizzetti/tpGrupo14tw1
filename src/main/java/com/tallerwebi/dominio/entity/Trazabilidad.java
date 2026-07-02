package com.tallerwebi.dominio.entity;

import java.time.OffsetDateTime;
import java.util.List;
import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Trazabilidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private OffsetDateTime fechaGeneracion;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "idProducto")
  private Producto producto;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "idTimer")
  private Timer timer;

  @OneToMany(mappedBy = "trazabilidad", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<TrazabilidadDetalle> articulosUsados;
}
