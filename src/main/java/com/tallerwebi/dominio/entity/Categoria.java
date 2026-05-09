package com.tallerwebi.dominio.entity;

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
public class Categoria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String icono;
  private Boolean estaActiva;
  private String nombre;

  @ManyToMany(mappedBy = "categorias")
  private List<Producto> productos = new ArrayList<>();

  public Categoria(String icono, Boolean estaActiva, String nombre) {
    this.icono = icono;
    this.nombre = nombre;
    this.estaActiva = estaActiva;
  }
}
