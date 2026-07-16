package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.EstadoComanda;
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
public class Comanda {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "pedidoId")
  private Pedido pedido;

  @Enumerated(EnumType.STRING)
  private EstadoComanda estado;

  @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ComandaSector> sectores = new ArrayList<>();
}
