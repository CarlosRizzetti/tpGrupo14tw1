package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.*;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.presentacion.controller.ControladorDashboard;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.hibernate.annotations.common.util.StringHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorDashboardTest {

  private HttpSession sessionMock;
  private ServicioDashboard servicioDashboardMock;
  private ControladorDashboard controladorDashboard;

  @BeforeEach
  public void init() {
    servicioDashboardMock = mock(ServicioDashboard.class);
    sessionMock = mock(HttpSession.class);
    controladorDashboard = new ControladorDashboard(servicioDashboardMock);
  }

  @Test
  public void queSeEnvienCorrectamenteLosTimersALaVistaCuandoNoSonNull() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    String fechaCreacionISo = fechaCreacion.toString();
    String fechaVencimientoISO = fechaCreacion.plusDays(3).toString();
    Categoria categoria = new Categoria("mccafe.png", true, "mccafe");
    categoria.setId(1L);
    CategoriaDto categoriaDTO = new CategoriaDto(categoria);
    String nombre = "hamburguesa";
    String ubicacion = "horno";
    TimerDTO timer = new TimerDTO(
      1L,
      nombre,
      "1AF34",
      fechaCreacionISo,
      fechaVencimientoISO,
      ubicacion
    );
    List<TimerDTO> timersActivos = List.of(timer);
    when(servicioDashboardMock.obtenerTimersActivos(anyLong())).thenReturn(timersActivos);
    when(sessionMock.getAttribute(anyString())).thenReturn(categoriaDTO);

    ModelAndView mav = controladorDashboard.index(sessionMock);
    Map<String, Object> model = mav.getModel();

    assertEquals(2, model.size());
    assertTrue(model.containsKey("timers"));
    List<TimerDTO> timers = (List<TimerDTO>) model.get("timers");
    assertEquals(1, timers.size());
    assertEquals(1L, timers.get(0).getId());
    assertEquals(timer, timers.get(0));
  }
}
