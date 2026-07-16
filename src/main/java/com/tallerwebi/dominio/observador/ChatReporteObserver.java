package com.tallerwebi.dominio.observador;

import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.evento.AdminActualizaReporteEvent;
import com.tallerwebi.dominio.evento.InteraccionChatEvent;
import com.tallerwebi.dominio.interfaces.ServicioPedido;
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class ChatReporteObserver {

  private static final Logger logger = LoggerFactory.getLogger(ChatReporteObserver.class);

  // Constantes para evitar errores de PMD (AvoidDuplicateLiterals)
  private static final String OPCION_ACTUALIZAR = "Actualizar reporte";
  private static final String OPCION_REALIZADO = "Dar por realizado";
  private static final String OPCION_DESESTIMAR = "Desestimar";
  private static final String OPCION_VOLVER = "Volver";

  private final ServicioPedido servicioPedido;

  public ChatReporteObserver(final ServicioPedido servicioPedido) {
    this.servicioPedido = servicioPedido;
  }

  // Escucha lo que hace el CLIENTE
  @EventListener
  public void onAccionCliente(final InteraccionChatEvent event) {
    final Pedido pedido = servicioPedido.buscarPedidoPorId(event.getPedidoId());
    if (pedido == null) {
      event.setRespuestaSistema("Error: No se encontró el pedido.");
      return;
    }

    final String accion = event.getAccionUsuario();

    if (accion.startsWith("TEXTO:")) {
      final String nuevoComentario = accion.substring(6).trim();
      pedido.setComentarioReclamo(nuevoComentario);

      event.setRespuestaSistema(
        "Hemos actualizado el detalle de tu reclamo a: '" +
        nuevoComentario +
        "'. ¿Cómo deseas continuar?"
      );
      event.setOpcionesDisponibles(
        Arrays.asList(OPCION_ACTUALIZAR, OPCION_REALIZADO, OPCION_DESESTIMAR)
      );

      servicioPedido.actualizarPedido(pedido);
      return;
    }

    switch (accion) {
      case "INICIAR":
        pedido.setReportado(true);
        event.setRespuestaSistema(
          "Tu reporte ha sido abierto y está siendo analizado. ¿Cómo quieres continuar?"
        );
        event.setOpcionesDisponibles(
          Arrays.asList(OPCION_ACTUALIZAR, OPCION_REALIZADO, "Desestimar reporte")
        );
        break;
      case "ACTUALIZAR":
        event.setRespuestaSistema("Por favor, escribe el detalle adicional de tu reclamo abajo.");
        event.setOpcionesDisponibles(Arrays.asList(OPCION_VOLVER, "Cancelar"));
        break;
      case "REALIZADO":
        pedido.setReportado(false);
        pedido.setComentarioReclamo("Resuelto por el cliente desde el chat.");
        event.setRespuestaSistema(
          "¡Perfecto! Hemos marcado este reporte como solucionado. Gracias por avisarnos."
        );
        break;
      case "DESESTIMAR":
        pedido.setReportado(false);
        pedido.setComentarioReclamo("Desestimado por el cliente.");
        event.setRespuestaSistema("Has desestimado este reporte. Quedará archivado sin impacto.");
        break;
      case "VOLVER":
      case "CANCELAR":
        event.setRespuestaSistema("Has cancelado la operación. ¿Qué deseas hacer ahora?");
        event.setOpcionesDisponibles(
          Arrays.asList(OPCION_ACTUALIZAR, OPCION_REALIZADO, OPCION_DESESTIMAR)
        );
        break;
      default:
        event.setRespuestaSistema("Acción no reconocida.");
        event.setOpcionesDisponibles(Arrays.asList(OPCION_VOLVER));
        break;
    }

    servicioPedido.actualizarPedido(pedido);
  }

  // Escucha lo que hace el ADMIN (Se mantiene igual, solo le agregué final)
  @EventListener
  public void onAccionAdmin(final AdminActualizaReporteEvent event) {
    final Pedido pedido = servicioPedido.buscarPedidoPorId(event.getReporteId());

    if (pedido != null) {
      pedido.setComentarioReclamo(
        "Un administrador ha cambiado el estado de tu reporte a: " + event.getNuevoEstado()
      );

      if ("RESUELTO".equalsIgnoreCase(event.getNuevoEstado())) {
        pedido.setReportado(false);
      }

      servicioPedido.actualizarPedido(pedido);
    }

    if (logger.isInfoEnabled()) {
      logger.info(
        "OBSERVER: Notificando al cliente del reporte ID {}. Mensaje: 'Un administrador ha cambiado el estado de tu reporte a: {}' | Opciones: Dar por realizado,Apelar decisión",
        event.getReporteId(),
        event.getNuevoEstado()
      );
    }
  }
}
