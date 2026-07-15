package com.tallerwebi.dominio.evento;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InteraccionChatEvent {
    private final Long reporteId; // ID del reporte/pedido
    private final Long clienteId; // ID del cliente
    private final String accionUsuario; // Ej: "INICIAR", "REALIZADO", "DESESTIMAR"

    public InteraccionChatEvent(Long reporteId, Long clienteId, String accionUsuario) {
        this.reporteId = reporteId;
        this.clienteId = clienteId;
        this.accionUsuario = accionUsuario;
    }
}