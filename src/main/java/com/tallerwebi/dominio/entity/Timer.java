package com.tallerwebi.dominio.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
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

  public Timer(
    OffsetDateTime fechaCreacion,
    OffsetDateTime fechaVencimiento,
    OffsetDateTime descongelamiento,
    Producto producto,
    Categoria categoria,
    ReglaVencimiento reglaVencimiento
  ) {
    this.fechaCreacion = fechaCreacion;
    this.fechaVencimiento = fechaVencimiento;
    this.descongelamiento = descongelamiento;
    this.producto = producto;
    this.categoria = categoria;
    this.reglaVencimiento = reglaVencimiento;
    this.estaActivo = true;
    this.estado = "activo";
  }

  @ManyToOne
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
