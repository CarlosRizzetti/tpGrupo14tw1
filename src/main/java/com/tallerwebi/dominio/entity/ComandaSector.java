package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.EstadoComandaSector;
import java.time.OffsetDateTime;
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
public class ComandaSector {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "comandaId")
  private Comanda comanda;

  @ManyToOne
  @JoinColumn(name = "categoriaId")
  private Categoria categoria;

  @Enumerated(EnumType.STRING)
  private EstadoComandaSector estado;

  private OffsetDateTime horaServido;
}
