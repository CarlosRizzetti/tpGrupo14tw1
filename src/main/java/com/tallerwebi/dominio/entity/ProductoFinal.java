package com.tallerwebi.dominio.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ProductoFinal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  private BigDecimal precio;

  @OneToMany(
    mappedBy = "productoFinal",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER
  )
  private List<ProductoFinalIngrediente> ingredientes = new ArrayList<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "ProductoFinalCategoria",
    joinColumns = @JoinColumn(name = "idProductoFinal"),
    inverseJoinColumns = @JoinColumn(name = "idCategoria")
  )
  private Set<Categoria> categorias = new TreeSet<>();

  public ProductoFinal(String nombre, BigDecimal precio) {
    this.nombre = nombre;
    this.precio = precio;
  }

  public void agregarIngrediente(Producto producto, int cantidad) {
    ProductoFinalIngrediente pfi = new ProductoFinalIngrediente();
    pfi.setProductoFinal(this);
    pfi.setProducto(producto);
    pfi.setCantidad(cantidad);
    this.ingredientes.add(pfi);
  }
}
