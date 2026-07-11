package com.tallerwebi.dominio;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import com.tallerwebi.dominio.services.ServicioTelegramImpl;
import com.tallerwebi.presentacion.dto.NotificacionVencimientoDto;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Pruebas unitarias para el servicio de Telegram.
 */
public class ServicioTelegramImplTest {

  private ServicioTelegramImpl servicioTelegram;
  private ServicioArticulo servicioArticuloMock;
  private RestTemplate restTemplateMock;
  private RepositorioTimer repositorioTimerMock;

  /**
   * Inicialización de objetos simulados antes de cada prueba.
   */
  @BeforeEach
  public void init() {
    servicioArticuloMock = mock(ServicioArticulo.class);
    restTemplateMock = mock(RestTemplate.class);
    repositorioTimerMock = mock(RepositorioTimer.class);
    servicioTelegram =
      new ServicioTelegramImpl(servicioArticuloMock, restTemplateMock, repositorioTimerMock);

    ReflectionTestUtils.setField(servicioTelegram, "botToken", "test-token");
    ReflectionTestUtils.setField(servicioTelegram, "chatId", "test-chat-id");
  }

  /**
   * Verifica que enviarMensaje realice una petición POST al endpoint correcto de Telegram.
   */
  @Test
  public void enviarMensajeDeberiaLlamarAlRestTemplate() {
    String mensaje = "Test message";
    servicioTelegram.enviarMensaje(mensaje);

    verify(restTemplateMock, times(1))
      .postForEntity(
        eq("https://api.telegram.org/bottest-token/sendMessage"),
        any(HttpEntity.class),
        eq(String.class)
      );
  }

  /**
   * Verifica que enviarNotificacionesVencimiento envíe un mensaje a Telegram cuando hay
   * artículos con exactamente 2, 5, 9 o 10 días restantes para su vencimiento.
   */
  @Test
  public void enviarNotificacionesVencimientoDeberiaEnviarMensajeCuandoHayProductosConDiasEspecificos() {
    List<NotificacionVencimientoDto> notifs = new ArrayList<>();

    Articulos a1 = new Articulos();
    a1.setNombre("Articulo 1");
    a1.setNumeroDeLote(123L);
    notifs.add(new NotificacionVencimientoDto(a1, 5, "MEDIA"));

    Articulos a2 = new Articulos();
    a2.setNombre("Articulo 2");
    a2.setNumeroDeLote(456L);
    notifs.add(new NotificacionVencimientoDto(a2, 2, "ALTA"));

    Articulos a3 = new Articulos();
    a3.setNombre("Articulo 3");
    notifs.add(new NotificacionVencimientoDto(a3, 3, "ALTA")); // No reportar (no es 2, 5, 9 o 10)

    when(servicioArticuloMock.obtenerNotificacionesVencimiento()).thenReturn(notifs);

    servicioTelegram.enviarNotificacionesVencimiento();

    verify(restTemplateMock, times(1))
      .postForEntity(
        eq("https://api.telegram.org/bottest-token/sendMessage"),
        any(HttpEntity.class),
        eq(String.class)
      );
  }

  /**
   * Verifica que enviarNotificacionesVencimiento no realice ninguna petición a Telegram si ningún
   * artículo coincide con el criterio de días específicos (2, 5, 9 o 10 días).
   */
  @Test
  public void enviarNotificacionesVencimientoNoDeberiaEnviarMensajeSiNoHayProductosConDiasEspecificos() {
    List<NotificacionVencimientoDto> notifs = new ArrayList<>();

    Articulos a1 = new Articulos();
    a1.setNombre("Articulo 1");
    notifs.add(new NotificacionVencimientoDto(a1, 3, "ALTA"));

    Articulos a2 = new Articulos();
    a2.setNombre("Articulo 2");
    notifs.add(new NotificacionVencimientoDto(a2, 7, "MEDIA"));

    when(servicioArticuloMock.obtenerNotificacionesVencimiento()).thenReturn(notifs);

    servicioTelegram.enviarNotificacionesVencimiento();

    verify(restTemplateMock, never())
      .postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
  }
}
