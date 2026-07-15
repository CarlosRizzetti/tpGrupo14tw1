package com.tallerwebi.dominio.evento;

public class AdminActualizaReporteEvent {
    private final Long reporteId;
    private final String nuevoEstado; // Ej: "RESUELTO"

    public AdminActualizaReporteEvent(Long reporteId, String nuevoEstado) {
        this.reporteId = reporteId;
        this.nuevoEstado = nuevoEstado;
    }
}