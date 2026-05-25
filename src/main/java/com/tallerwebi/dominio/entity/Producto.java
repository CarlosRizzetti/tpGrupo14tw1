package com.tallerwebi.dominio.entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Producto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  private Boolean estaActivo;

  @ManyToMany
  @JoinTable(
    name = "productosCategoria",
    joinColumns = @JoinColumn(name = "idProducto"),
    inverseJoinColumns = @JoinColumn(name = "idCategoria")
  )
  private Set<Categoria> categorias = new HashSet<>();

  @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<ReglaVencimiento> reglas = new HashSet<>();
}
