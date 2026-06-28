package com.tallerwebi.dominio.entity;

import com.tallerwebi.dominio.entity.enums.EstadoUsuario;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    name = "usuario_categorias_relacion",
    joinColumns = @JoinColumn(name = "usuario_id"),
    inverseJoinColumns = @JoinColumn(name = "categoria_id")
  )
  private Set<Categoria> categorias = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public String getTokenValidacion() {
    return tokenValidacion;
  }

  public void setTokenValidacion(String tokenValidacion) {
    this.tokenValidacion = tokenValidacion;
  }
}