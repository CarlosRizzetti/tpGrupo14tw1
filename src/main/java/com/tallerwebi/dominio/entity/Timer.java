package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.embeddables.CicloVida;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

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

  @Embedded
  private CicloVida cicloVida;

  private Integer cantidadProducto;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "idUsuario")
  @NotFound(action = NotFoundAction.IGNORE)
  private Usuario usuario;

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
    Integer cantidadProducto,
    Usuario usuario
  ) {
    this.cicloVida = new CicloVida(fechaCreacion, fechaVencimiento);
    this.groupId = groupId;
    this.estado = EstadoTimer.ACTIVO;
    this.producto = producto;
    this.categoria = categoria;
    this.reglaVencimiento = reglaVencimiento;
    this.cantidadProducto = cantidadProducto;
    this.usuario = usuario;
  }

  public Timer(
    OffsetDateTime fechaCreacion,
    OffsetDateTime fechaVencimiento,
    OffsetDateTime descongelamiento,
    Producto producto,
    Categoria categoria,
    ReglaVencimiento reglaVencimiento,
    Integer cantidadProducto,
    Usuario usuario
  ) {
    this.cicloVida = new CicloVida(fechaCreacion, fechaVencimiento, descongelamiento);
    this.producto = producto;
    this.categoria = categoria;
    this.reglaVencimiento = reglaVencimiento;
    this.estado = EstadoTimer.ACTIVO;
    this.cantidadProducto = cantidadProducto;
    this.usuario = usuario;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "idProducto")
  @NotFound(action = NotFoundAction.IGNORE)
  private Producto producto;

  @ManyToOne
  @JoinColumn(name = "idCategoria")
  @NotFound(action = NotFoundAction.IGNORE)
  private Categoria categoria;

  @ManyToOne
  @JoinColumn(name = "idReglaVencimiento")
  @NotFound(action = NotFoundAction.IGNORE)
  private ReglaVencimiento reglaVencimiento;

  @PrePersist
  public void setGroupId() {
    if (this.groupId == null || this.groupId.isEmpty()) {
      this.groupId = UUID.randomUUID().toString();
    }
  }
}
