package com.tallerwebi.dominio.interfaces;

/**
 * Servicio para gestionar el envío de notificaciones y mensajes a Telegram.
 */
public interface ServicioTelegram {
  /**
   * Envía un mensaje de texto al chat de Telegram configurado.
   *
   * @param mensaje el mensaje a enviar
   */
  void enviarMensaje(String mensaje);

  /**
   * Verifica los artículos próximos a vencerse y envía una notificación a Telegram
   * si su fecha de vencimiento está a 2, 5, 9 o 10 días restantes.
   */
  void enviarNotificacionesVencimiento();

  /**
   * Tarea programada que verifica si hay timers activos a menos de 2 minutos de vencerse
   * y envía una alerta a Telegram.
   */
  void verificarTimersPorVencer();
}
