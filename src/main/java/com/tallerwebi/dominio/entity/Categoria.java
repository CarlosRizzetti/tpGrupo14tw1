package com.tallerwebi.dominio.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Categoria implements Comparable<Categoria> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String icono;
  private Boolean estaActiva;
  private String nombre;

  @ManyToMany(mappedBy = "categorias")
  private List<Producto> productos = new ArrayList<>();

  @ManyToMany(mappedBy = "categorias")
  private List<Usuario> usuarios = new ArrayList<>();

  public Categoria(String icono, Boolean estaActiva, String nombre) {
    this.icono = icono;
    this.nombre = nombre;
    this.estaActiva = estaActiva;
  }

  @Override
  public int compareTo(Categoria otra) {
    return Long.compare(this.id, otra.id);
  }
}
