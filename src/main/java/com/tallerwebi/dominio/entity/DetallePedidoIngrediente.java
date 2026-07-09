package com.tallerwebi.dominio.entity;

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
public class DetallePedidoIngrediente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "detallePedidoId")
  private DetallePedido detallePedido;

  @ManyToOne
  @JoinColumn(name = "productoId")
  private Producto producto;

  private Integer cantidad;

  // Vacío hasta que cocina "saca" la comanda y se registra qué timer(s) se consumieron
  @OneToMany(mappedBy = "detallePedidoIngrediente", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ConsumoTimer> consumos = new ArrayList<>();
}
