package com.tallerwebi.dominio.observador;

import com.tallerwebi.dominio.evento.AdminActualizaReporteEvent;
import com.tallerwebi.dominio.evento.InteraccionChatEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
// Importa tus servicios y repositorios aquí...

@Component
public class ChatReporteObserver {

    // Aquí inyectarías el ServicioReporte para interactuar con la BD
    // private final ServicioReporte servicioReporte;

    // 1. Escucha lo que hace el CLIENTE
    @EventListener
    public void onAccionCliente(InteraccionChatEvent event) {
        String mensajeSistema = "";
        String opcionesDisponibles = ""; // Separadas por coma o en formato JSON

        switch (event.getAccionUsuario()) {
            case "INICIAR":
                mensajeSistema = "Tu reporte está siendo analizado. ¿Cómo quieres continuar?";
                opcionesDisponibles = "Actualizar reporte,Dar por realizado,Desestimar reporte";
                break;
            case "ACTUALIZAR":
                mensajeSistema = "Por favor, escribe el detalle adicional abajo.";
                opcionesDisponibles = "Volver,Cancelar";
                break;
            case "REALIZADO":
                mensajeSistema = "¡Perfecto! Hemos marcado este reporte como solucionado. Gracias por avisarnos.";
                opcionesDisponibles = "NINGUNA"; // Fin del chat
                break;
            case "DESESTIMAR":
                mensajeSistema = "Has desestimado este reporte. Quedará archivado sin impacto.";
                opcionesDisponibles = "NINGUNA"; // Fin del chat
                break;
        }

        // TODO: Guardar este "mensajeSistema" y las "opcionesDisponibles" en la Base de Datos
        // asociadas al event.getReporteId() para que el Controlador pueda leerlas.
        System.out.println("OBSERVER: Guardando respuesta automática para el reporte " + event.getReporteId());
    }

    // 2. Escucha lo que hace el ADMIN
    @EventListener
    public void onAccionAdmin(AdminActualizaReporteEvent event) {
        String mensajeSistema = "🔔 Un administrador ha cambiado el estado de tu reporte a: " + event.getNuevoEstado();
        String opcionesDisponibles = "Dar por realizado,Apelar decisión";

        // TODO: Guardar en BD para que cuando el cliente entre al chat lo vea.
        System.out.println("OBSERVER: Notificando al cliente del reporte " + event.getReporteId());
    }
}