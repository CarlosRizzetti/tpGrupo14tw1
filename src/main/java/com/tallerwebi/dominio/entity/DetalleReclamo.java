package com.tallerwebi.dominio.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetalleReclamo {

  @Column(columnDefinition = "boolean default false")
  private Boolean reportado = false;

  private String motivoReclamo;

  private String comentarioReclamo;
}
