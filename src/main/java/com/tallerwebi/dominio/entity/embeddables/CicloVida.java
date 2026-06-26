package com.tallerwebi.dominio.entity.embeddables;

import java.time.OffsetDateTime;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CicloVida {

  private OffsetDateTime fechaCreacion;
  private OffsetDateTime fechaVencimiento;
  private OffsetDateTime descongelamiento;

  public CicloVida(OffsetDateTime fechaCreacion, OffsetDateTime fechaVencimiento) {
    this.fechaCreacion = fechaCreacion;
    this.fechaVencimiento = fechaVencimiento;
  }
}
