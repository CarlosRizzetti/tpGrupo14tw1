package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.dominio.services.ServicioDashboardImpl;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioDashboardTest {

  public ServicioDashboard servicioDashboard;
  public RepositorioTimer repositorioTimerMock;
  public RepositorioCategoria repositorioCategoriaMock;

  @BeforeEach
  public void init() {
    this.repositorioTimerMock = mock(RepositorioTimer.class);
    this.repositorioCategoriaMock = mock(RepositorioCategoria.class);
    this.servicioDashboard =
      new ServicioDashboardImpl(repositorioTimerMock, repositorioCategoriaMock);
  }

  @Test
  public void queDevuelvaTodasLosTimersActivos() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusDays(3);
    Categoria categoria = new Categoria("mccafe.png", true, "mccafe");
    Producto producto = new Producto();
    ReglaVencimiento regla = new ReglaVencimiento();
    Timer timer = new Timer(fechaCreacion, fechaVencimiento, "1AF34", producto, categoria, regla);
    timer.setId(1L);
    List<Timer> timersActivos = List.of(timer);
    when(repositorioTimerMock.obtenerTimersSegunEstado(categoria.getId(), "activo"))
      .thenReturn(timersActivos);

    List<TimerDTO> listaObtenida = this.servicioDashboard.obtenerTimersActivos(categoria.getId());

    assertEquals(1, listaObtenida.size());
    verify(repositorioTimerMock, times(1)).obtenerTimersSegunEstado(categoria.getId(), "activo");
    assertEquals(1L, listaObtenida.get(0).getId());
  }

  @Test
  public void queNoHagaNadaSiElTimerNoExiste() {
    when(repositorioTimerMock.buscarPorId(99L)).thenReturn(null);

    this.servicioDashboard.eliminarTimer(99L);

    verify(repositorioTimerMock, never()).guardar(any());
  }

  @Test
  public void queElTimerQuedeMarcadoComoVencidoSiYaVencio() {
    OffsetDateTime fechaVencimientoPasada = OffsetDateTime.now().minusHours(1);
    Timer timer = new Timer()
      .builder()
      .fechaCreacion(OffsetDateTime.now().minusHours(2))
      .fechaVencimiento(fechaVencimientoPasada)
      .build();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);

    servicioDashboard.eliminarTimer(1L);

    assertEquals("vencido", timer.getEstado());
    assertFalse(timer.getEstaActivo());
    verify(repositorioTimerMock).guardar(timer);
  }

  @Test
  public void queElTimerQuedeMarcadoComoEliminadoSiNoVencioTodavia() {
    OffsetDateTime fechaVencimientoFutura = OffsetDateTime.now().plusHours(1);
    Timer timer = new Timer()
      .builder()
      .fechaCreacion(OffsetDateTime.now())
      .fechaVencimiento(fechaVencimientoFutura)
      .build();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);

    servicioDashboard.eliminarTimer(1L);

    assertEquals("eliminado", timer.getEstado());
    assertFalse(timer.getEstaActivo());
    verify(repositorioTimerMock).guardar(timer);
  }
}
