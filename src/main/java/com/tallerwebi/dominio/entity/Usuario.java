package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.EstadoUsuario;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

  private String email;
  private String password;
  private String nombre;
  private String rol;
  private String tokenValidacion;

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Timer> timers;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoUsuario estado = EstadoUsuario.PENDIENTE;

  @ManyToMany
  @JoinTable(
    name = "usuarioCategorias",
    joinColumns = @JoinColumn(name = "idUsuario"),
    inverseJoinColumns = @JoinColumn(name = "idCategoria")
  )
  private Set<Categoria> categorias = new TreeSet<>();

  public String getCategoriasIds() {
    if (categorias == null || categorias.isEmpty()) return "";
    return categorias.stream().map(c -> String.valueOf(c.getId())).collect(Collectors.joining(","));
  }
}
