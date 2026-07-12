package com.tallerwebi.dominio.entity;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

  @Column(nullable = false)
  private Integer cantidad = 0;

  @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Timer> timers;

  @ManyToMany
  @JoinTable(
    name = "ProductosCategoria",
    joinColumns = @JoinColumn(name = "idProducto"),
    inverseJoinColumns = @JoinColumn(name = "idCategoria")
  )
  private Set<Categoria> categorias = new TreeSet<>();

  @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<ReglaVencimiento> reglas = new TreeSet<>();
}
