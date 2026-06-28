package com.tallerwebi.dominio.entity;

import java.util.List;
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
  private String nombre;
  private String rol;
  private String tokenValidacion;

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Timer> timers;

  private Boolean activo = false;

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
