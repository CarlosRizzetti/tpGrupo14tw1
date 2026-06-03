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

  public ServicioCategoria servicioCategoria;
  public ServicioDashboard servicioDashboard;

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
    try {
      ModelAndView mav = new ModelAndView("dashboard/global");

      List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();

      if (categorias == null || categorias.isEmpty()) {
        mav.addObject("error", "No hay categorías disponibles");
        return mav;
      }

      Map<Long, List<TimerDTO>> timersPorCategoria = new HashMap<>();
      boolean anyTimers = false;

      for (CategoriaDto categoria : categorias) {
        List<TimerDTO> timers = servicioDashboard.obtenerTimersActivos(categoria.getId());
        timersPorCategoria.put(categoria.getId(), timers);
        if (timers != null && !timers.isEmpty()) {
          anyTimers = true;
        }
      }

      mav.addObject("categorias", categorias);

      if (!anyTimers) {
        mav.addObject("error", "No hay timers activos");
      } else {
        mav.addObject("timersPorCategoria", timersPorCategoria);
      }

      return mav;
    } catch (Exception e) {
      ModelAndView mav = new ModelAndView("dashboard/global");
      mav.addObject("error", "Error al cargar el dashboard global");
      return mav;
    }
  }
}
