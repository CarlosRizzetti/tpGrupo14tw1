package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Producto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  private Boolean esta_activo;

  @ManyToMany
  @JoinTable(
    name = "productos_categoria",
    joinColumns = @JoinColumn(name = "id_producto"),
    inverseJoinColumns = @JoinColumn(name = "id_categoria")
  )
  private List<Categoria> categorias = new ArrayList<>();
}
