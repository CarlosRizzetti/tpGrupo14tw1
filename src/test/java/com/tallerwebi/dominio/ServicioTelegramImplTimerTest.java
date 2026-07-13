package com.tallerwebi.dominio;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.embeddables.CicloVida;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.services.ServicioTelegramImpl;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Pruebas unitarias para verificar la alerta de timers próximos a vencerse en ServicioTelegramImpl.
 */
public class ServicioTelegramImplTimerTest {

  private ServicioTelegramImpl servicioTelegram;
  private ServicioLote servicioLoteMock;
  private RestTemplate restTemplateMock;
  private RepositorioTimer repositorioTimerMock;

  @BeforeEach
  public void init() {
    servicioLoteMock = mock(ServicioLote.class);
    restTemplateMock = mock(RestTemplate.class);
    repositorioTimerMock = mock(RepositorioTimer.class);
    servicioTelegram =
      new ServicioTelegramImpl(servicioLoteMock, restTemplateMock, repositorioTimerMock);

    ReflectionTestUtils.setField(servicioTelegram, "botToken", "test-token");
    ReflectionTestUtils.setField(servicioTelegram, "chatId", "test-chat-id");
  }

  @Test
  public void verificarTimersPorVencerDeberiaEnviarMensajeCuandoUnTimerVenceEnMenosDeDosMinutos() {
    List<Timer> timers = new ArrayList<>();

    Producto producto = new Producto();
    producto.setNombre("Queso");

    Categoria categoria = new Categoria();
    categoria.setNombre("Fiambreria");

    // Vence en 90 segundos
    CicloVida ciclo = new CicloVida(OffsetDateTime.now(), OffsetDateTime.now().plusSeconds(90));

    Timer timer = new Timer();
    timer.setId(1L);
    timer.setProducto(producto);
    timer.setCategoria(categoria);
    timer.setCicloVida(ciclo);
    timer.setEstado(EstadoTimer.ACTIVO);
    timers.add(timer);

    when(repositorioTimerMock.obtenerTimersConFiltro(EstadoTimer.ACTIVO, null)).thenReturn(timers);

    servicioTelegram.verificarTimersPorVencer();

    verify(restTemplateMock, times(1))
      .postForEntity(
        eq("https://api.telegram.org/bottest-token/sendMessage"),
        any(HttpEntity.class),
        eq(String.class)
      );
  }

  @Test
  public void verificarTimersPorVencerNoDeberiaEnviarMensajeSiElTimerFaltaMasDeDosMinutos() {
    List<Timer> timers = new ArrayList<>();

    Producto producto = new Producto();
    producto.setNombre("Queso");

    Categoria categoria = new Categoria();
    categoria.setNombre("Fiambreria");

    // Vence en 5 minutos
    CicloVida ciclo = new CicloVida(OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(5));

    Timer timer = new Timer();
    timer.setId(1L);
    timer.setProducto(producto);
    timer.setCategoria(categoria);
    timer.setCicloVida(ciclo);
    timer.setEstado(EstadoTimer.ACTIVO);
    timers.add(timer);

    when(repositorioTimerMock.obtenerTimersConFiltro(EstadoTimer.ACTIVO, null)).thenReturn(timers);

    servicioTelegram.verificarTimersPorVencer();

    verify(restTemplateMock, never())
      .postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
  }

  @Test
  public void verificarTimersPorVencerNoDeberiaEnviarMensajeDuplicadoParaElMismoTimer() {
    List<Timer> timers = new ArrayList<>();

    Producto producto = new Producto();
    producto.setNombre("Queso");

    Categoria categoria = new Categoria();
    categoria.setNombre("Fiambreria");

    CicloVida ciclo = new CicloVida(OffsetDateTime.now(), OffsetDateTime.now().plusSeconds(90));

    Timer timer = new Timer();
    timer.setId(1L);
    timer.setProducto(producto);
    timer.setCategoria(categoria);
    timer.setCicloVida(ciclo);
    timer.setEstado(EstadoTimer.ACTIVO);
    timers.add(timer);

    when(repositorioTimerMock.obtenerTimersConFiltro(EstadoTimer.ACTIVO, null)).thenReturn(timers);

    // Primera ejecución
    servicioTelegram.verificarTimersPorVencer();

    // Segunda ejecución
    servicioTelegram.verificarTimersPorVencer();

    verify(restTemplateMock, times(1))
      .postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
  }
}
