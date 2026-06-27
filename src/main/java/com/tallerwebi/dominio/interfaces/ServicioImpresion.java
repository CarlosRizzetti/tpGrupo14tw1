package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import java.time.OffsetDateTime;

public interface ServicioImpresion {
  public void imprimirTicketVencimiento(
    Producto producto,
    ReglaVencimiento regla,
    OffsetDateTime fechaElaboracion,
    OffsetDateTime fechaVencimiento,
    OffsetDateTime fechaDescongelamiento
  );
}
