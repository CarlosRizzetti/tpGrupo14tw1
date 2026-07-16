package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "clienteId")
  private Cliente cliente;

  @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DetallePedido> detalles = new ArrayList<>();

  @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
  private Comanda comanda;

  private OffsetDateTime horaCobro;

  private OffsetDateTime horaSalida;

  private BigDecimal precioFinal;

  @Enumerated(EnumType.STRING)
  private EstadoPedido estado;

  @Embedded
  private DetalleReclamo detalleReclamo = new DetalleReclamo();

  public Boolean getReportado() {
    return detalleReclamo != null && Boolean.TRUE.equals(detalleReclamo.getReportado());
  }

  public void setReportado(Boolean reportado) {
    if (this.detalleReclamo == null) {
      this.detalleReclamo = new DetalleReclamo();
    }
    this.detalleReclamo.setReportado(reportado);
  }

  public String getMotivoReclamo() {
    return detalleReclamo != null ? detalleReclamo.getMotivoReclamo() : null;
  }

  public void setMotivoReclamo(String motivoReclamo) {
    if (this.detalleReclamo == null) {
      this.detalleReclamo = new DetalleReclamo();
    }
    this.detalleReclamo.setMotivoReclamo(motivoReclamo);
  }

  public String getComentarioReclamo() {
    return detalleReclamo != null ? detalleReclamo.getComentarioReclamo() : null;
  }

  public void setComentarioReclamo(String comentarioReclamo) {
    if (this.detalleReclamo == null) {
      this.detalleReclamo = new DetalleReclamo();
    }
    this.detalleReclamo.setComentarioReclamo(comentarioReclamo);
  }
}
