package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Timer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String groupId;
  private OffsetDateTime descongelamiento;
  private OffsetDateTime fechaCreacion;
  private OffsetDateTime fechaVencimiento;
  private Integer cantidadProducto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoTimer estado;

  public Timer(
    OffsetDateTime fechaCreacion,
    OffsetDateTime fechaVencimiento,
    String groupId,
    Producto producto,
    Categoria categoria,
    ReglaVencimiento reglaVencimiento,
    Integer cantidadProducto
  ) {
    this.fechaCreacion = fechaCreacion;
    this.fechaVencimiento = fechaVencimiento;
    this.groupId = groupId;
    this.estado = EstadoTimer.ACTIVO;
    this.producto = producto;
    this.categoria = categoria;
    this.reglaVencimiento = reglaVencimiento;
    this.cantidadProducto = cantidadProducto;
  }

  public Timer(
    OffsetDateTime fechaCreacion,
    OffsetDateTime fechaVencimiento,
    OffsetDateTime descongelamiento,
    Producto producto,
    Categoria categoria,
    ReglaVencimiento reglaVencimiento,
    Integer cantidadProducto
  ) {
    this.fechaCreacion = fechaCreacion;
    this.fechaVencimiento = fechaVencimiento;
    this.descongelamiento = descongelamiento;
    this.producto = producto;
    this.categoria = categoria;
    this.reglaVencimiento = reglaVencimiento;
    this.estado = EstadoTimer.ACTIVO;
    this.cantidadProducto = cantidadProducto;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "idProducto")
  private Producto producto;

  @ManyToOne
  @JoinColumn(name = "idCategoria")
  private Categoria categoria;

  @ManyToOne
  @JoinColumn(name = "idReglaVencimiento")
  private ReglaVencimiento reglaVencimiento;

  @PrePersist
  public void setGroupId() {
    if (this.groupId == null || this.groupId.isEmpty()) {
      this.groupId = UUID.randomUUID().toString();
    }
  }
}
