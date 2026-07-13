package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.interfaces.ServicioTelegram;
import com.tallerwebi.presentacion.dto.NotificacionVencimientoDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Implementación del servicio de Telegram para enviar notificaciones de
 * vencimiento y alertas de timers.
 */
@Service("servicioTelegram")
public class ServicioTelegramImpl implements ServicioTelegram {

  private static final Logger log = LoggerFactory.getLogger(ServicioTelegramImpl.class);
  private static final String NOT_AVAILABLE = "N/A";

  private final ServicioLote servicioLote;
  private final RestTemplate restTemplate;
  private final RepositorioTimer repositorioTimer;
  private final Set<Long> timersNotificados = ConcurrentHashMap.newKeySet();

  @Value("${telegram.bot.token:}")
  private String botToken;

  @Value("${telegram.chat.id:}")
  private String chatId;

  /**
   * Constructor del servicio.
   *
   * @param servicioLote el servicio de lotes
   * @param restTemplate     plantilla para consumo de APIs REST
   * @param repositorioTimer el repositorio de timers
   */
  @Autowired
  public ServicioTelegramImpl(
    ServicioLote servicioLote,
    RestTemplate restTemplate,
    RepositorioTimer repositorioTimer
  ) {
    this.servicioLote = servicioLote;
    this.restTemplate = restTemplate;
    this.repositorioTimer = repositorioTimer;
  }

  @Override
  public void enviarMensaje(String mensaje) {
    if (botToken == null || botToken.isEmpty() || chatId == null || chatId.isEmpty()) {
      if (log.isWarnEnabled()) {
        log.warn("Telegram bot token o chat ID no están configurados.");
      }
      return;
    }

    String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> body = new HashMap<>();
      body.put("chat_id", chatId);
      body.put("text", mensaje);

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
      restTemplate.postForEntity(url, entity, String.class);

      if (log.isInfoEnabled()) {
        log.info("Mensaje enviado exitosamente a Telegram.");
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Error al enviar mensaje a Telegram", e);
      }
    }
  }

  @Override
  @Scheduled(cron = "0 40 19 * * *") // Todos los días a las 9:00 AM
  public void enviarNotificacionesVencimiento() {
    List<NotificacionVencimientoDto> notificaciones =
      servicioLote.obtenerNotificacionesVencimiento();

    if (notificaciones == null || notificaciones.isEmpty()) {
      if (log.isInfoEnabled()) {
        log.info("No hay notificaciones de vencimiento para procesar.");
      }
      return;
    }

    String reporte = construirReporteDeVencimientos(notificaciones);

    if (!reporte.isEmpty()) {
      enviarMensaje(reporte);
    } else {
      if (log.isInfoEnabled()) {
        log.info("No se encontraron productos con 2, 5, 9 o 10 días restantes para vencer.");
      }
    }
  }

  private String construirReporteDeVencimientos(List<NotificacionVencimientoDto> notificaciones) {
    StringBuilder sb = new StringBuilder();
    for (NotificacionVencimientoDto notif : notificaciones) {
      long dias = notif.getDiasRestantes();
      if (debeNotificar(dias)) {
        agregarArticuloAlReporte(sb, notif, dias);
      }
    }
    return sb.toString();
  }

  private boolean debeNotificar(long dias) {
    return dias == 2 || dias == 5 || dias == 9 || dias == 10;
  }

  private void agregarArticuloAlReporte(
    StringBuilder sb,
    NotificacionVencimientoDto notif,
    long dias
  ) {
    if (sb.length() == 0) {
      sb.append("⚠️ Alerta de Vencimiento de Productos:\n");
    }
    String lote = notif.getLote().getNumeroDeLote() != null
      ? notif.getLote().getNumeroDeLote().toString()
      : NOT_AVAILABLE;
    sb.append(
      String.format(
        "- %s (Lote: %s) vence en %d días.\n",
        notif.getLote().getProducto().getNombre(),
        lote,
        dias
      )
    );
  }

  @Override
  @Scheduled(fixedRate = 15000)
  @Transactional(readOnly = true)
  public void verificarTimersPorVencer() {
    List<Timer> timersActivos = repositorioTimer.obtenerTimersConFiltro(EstadoTimer.ACTIVO, null);
    if (timersActivos == null || timersActivos.isEmpty()) {
      return;
    }
    for (Timer timer : timersActivos) {
      procesarTimer(timer);
    }
  }

  private void procesarTimer(Timer timer) {
    if (timer.getCicloVida() == null || timer.getCicloVida().getFechaVencimiento() == null) {
      return;
    }
    java.time.OffsetDateTime ahora = java.time.OffsetDateTime.now();
    java.time.OffsetDateTime vto = timer.getCicloVida().getFechaVencimiento();
    java.time.Duration duration = java.time.Duration.between(ahora, vto);
    long segundosRestantes = duration.getSeconds();
    if (segundosRestantes > 0 && segundosRestantes < 120 && timersNotificados.add(timer.getId())) {
      long min = segundosRestantes / 60;
      long seg = segundosRestantes % 60;
      String productoNombre = timer.getProducto() != null
        ? timer.getProducto().getNombre()
        : NOT_AVAILABLE;
      String categoriaNombre = timer.getCategoria() != null
        ? timer.getCategoria().getNombre()
        : NOT_AVAILABLE;
      String mensaje = String.format(
        "⚠️ ¡Timer por vencer! El producto '%s' (Categoría: %s) está a punto de vencerse. Tiempo restante: %d min %d seg.",
        productoNombre,
        categoriaNombre,
        min,
        seg
      );
      enviarMensaje(mensaje);
    }
  }
}
