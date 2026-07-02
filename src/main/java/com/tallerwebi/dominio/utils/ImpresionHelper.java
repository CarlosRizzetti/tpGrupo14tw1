package com.tallerwebi.dominio.utils;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.ServicioImpresion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpresionHelper {

  private static final Logger log = LoggerFactory.getLogger(ImpresionHelper.class);

  public static void intentarImpresionDeVencimiento(
    Timer timer,
    ServicioImpresion servicioImpresion
  ) {
    try {
      servicioImpresion.imprimirTicketVencimiento(
        timer.getProducto(),
        timer.getReglaVencimiento(),
        timer.getCicloVida().getFechaCreacion(),
        timer.getCicloVida().getFechaVencimiento(),
        timer.getCicloVida().getDescongelamiento()
      );
    } catch (Exception e) {
      if (log.isWarnEnabled()) {
        log.warn(
          "No se pudo imprimir el ticket de vencimiento para el timer {}: {}",
          timer.getId(),
          e.getMessage()
        );
      }
    }
  }
}
