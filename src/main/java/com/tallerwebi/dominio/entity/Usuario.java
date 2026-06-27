package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.EstadoUsuario;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String email;

  private String password;
  private String rol;
  private String tokenValidacion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoUsuario estado = EstadoUsuario.PENDIENTE;

  @ManyToMany
  @JoinTable(
    name = "usuario_categorias_relacion",
    joinColumns = @JoinColumn(name = "usuario_id"),
    inverseJoinColumns = @JoinColumn(name = "categoria_id")
  )
  private Set<Categoria> categorias = new HashSet<>();

  public String getCategoriasIds() {
    if (categorias == null || categorias.isEmpty()) return "";
    return categorias.stream().map(c -> String.valueOf(c.getId())).collect(Collectors.joining(","));
  }
}
