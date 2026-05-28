package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorDashboardGlobal {

  private final ServicioCategoria servicioCategoria;
  private final ServicioDashboard servicioDashboard;

  @Autowired
  public ControladorDashboardGlobal(
    ServicioCategoria servicioCategoria,
    ServicioDashboard servicioDashboard
  ) {
    this.servicioCategoria = servicioCategoria;
    this.servicioDashboard = servicioDashboard;
  }

  @GetMapping("/dashboard/global")
  public ModelAndView dashboardGlobal() {
    ModelAndView mav = new ModelAndView("dashboard/global");

    List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();

    Map<Long, List<TimerDTO>> timersPorCategoria = new HashMap<>();

    for (CategoriaDto categoria : categorias) {
      List<TimerDTO> timers = servicioDashboard.obtenerTimersActivos(categoria.getId());

      timersPorCategoria.put(categoria.getId(), timers);
    }

    mav.addObject("categorias", categorias);
    mav.addObject("timersPorCategoria", timersPorCategoria);

    return mav;
  }
}
