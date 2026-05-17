package com.tallerwebi.dominio.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Timer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String groupId;

  private OffsetDateTime fechaCreacion;
  private OffsetDateTime fechaVencimiento;
  private Boolean estaActivo;
  private String estado;

  public Timer(
    OffsetDateTime fechaCreacion,
    OffsetDateTime fechaVencimiento,
    String groupId,
    Producto producto,
    Categoria categoria,
    ReglaVencimiento reglaVencimiento
  ) {
    this.fechaCreacion = fechaCreacion;
    this.fechaVencimiento = fechaVencimiento;
    this.groupId = groupId;
    this.estaActivo = true;
    this.estado = "activo";
    this.producto = producto;
    this.categoria = categoria;
    this.reglaVencimiento = reglaVencimiento;
  }

  public Timer(OffsetDateTime fechaCreacion, OffsetDateTime fechaVencimiento) {
    this.fechaCreacion = fechaCreacion;
    this.fechaVencimiento = fechaVencimiento;
    this.estaActivo = true;
    this.estado = "activo";
  }

  @ManyToOne
  @JoinColumn(name = "id_producto")
  private Producto producto;

  @ManyToOne
  @JoinColumn(name = "id_categoria")
  private Categoria categoria;

  @ManyToOne
  @JoinColumn(name = "id_regla_vencimiento")
  private ReglaVencimiento reglaVencimiento;

  @PrePersist
  public void setGroupId() {
    if (this.groupId == null || this.groupId.isEmpty()) {
      this.groupId = UUID.randomUUID().toString();
    }
  }
}
