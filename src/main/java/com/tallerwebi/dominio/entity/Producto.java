package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.TipoProducto;
import com.tallerwebi.dominio.entity.enums.UnidadDeMedida;
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

  @Enumerated(EnumType.STRING)
  private UnidadDeMedida unidadDeMedida;

  @Enumerated(EnumType.STRING)
  private TipoProducto tipoProducto;

  @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Timer> timers;

  @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Lote> lotes;

  @ManyToMany
  @JoinTable(
    name = "ProductosCategoria",
    joinColumns = @JoinColumn(name = "idProducto"),
    inverseJoinColumns = @JoinColumn(name = "idCategoria")
  )
  private Set<Categoria> categorias = new TreeSet<>();

  @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<ReglaVencimiento> reglas = new TreeSet<>();

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof Producto)) return false;
    Producto producto = (Producto) object;
    return id != null && id.equals(producto.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
