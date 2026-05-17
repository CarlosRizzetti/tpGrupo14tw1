package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.dominio.services.ServicioDashboardImpl;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioDashboardTest {

  public ServicioDashboard servicioDashboard;
  public RepositorioTimer repositorioTimerMock;

  @BeforeEach
  public void init() {
    this.repositorioTimerMock = mock(RepositorioTimer.class);
    this.servicioDashboard = new ServicioDashboardImpl(repositorioTimerMock);
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

    List<Timer> listaObtenida = this.servicioDashboard.obtenerTimersActivos(categoria.getId());

    assertEquals(1, listaObtenida.size());
    verify(repositorioTimerMock, times(1)).obtenerTimersSegunEstado(categoria.getId(), "activo");
    assertEquals(1L, listaObtenida.get(0).getId());
    assertEquals(timer, listaObtenida.get(0));
  }
}
