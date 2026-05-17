package com.tallerwebi.dominio.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
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

  private Boolean estaActivo;

  @ManyToMany
  @JoinTable(
    name = "productosCategoria",
    joinColumns = @JoinColumn(name = "idProducto"),
    inverseJoinColumns = @JoinColumn(name = "idCategoria")
  )
  private List<Categoria> categorias = new ArrayList<>();

  @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL)
  private ReglaVencimiento reglaVencimiento;
}
