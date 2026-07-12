package com.tallerwebi.dominio.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cliente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  @Column(unique = true)
  private String documento;

  private String telefono;

  @Column(unique = true)
  private String email;

  private String password;

  @Override
  public String toString() {
    if (email != null && !email.trim().isEmpty()) {
      return email;
    }
    if (documento != null && !documento.trim().isEmpty()) {
      return documento;
    }
    return nombre != null ? nombre : "Cliente";
  }
}
