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

  @Column(columnDefinition = "boolean default false")
  private Boolean reportado = false;

  public Boolean getReportado() {
    return Boolean.TRUE.equals(reportado);
  }
}
