package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorDashboard {

  public ServicioDashboard servicioDashboard;

  @Autowired
  public ControladorDashboard(ServicioDashboard servicioDashboard) {
    this.servicioDashboard = servicioDashboard;
  }

  @GetMapping("/dashboard")
  public ModelAndView index(HttpSession session) {
    CategoriaDto categoria = (CategoriaDto) session.getAttribute("categoria");

    if (categoria == null) {
      return new ModelAndView("redirect:/home");
    }

    ModelAndView mav = new ModelAndView("dashboard");
    mav.addObject("categoria", categoria);
    List<TimerDTO> timersActivos = servicioDashboard.obtenerTimersActivos(categoria.getId());

    if (timersActivos.isEmpty()) {
      mav.addObject("error", "No hay timers activos");
    } else {
      mav.addObject("timers", timersActivos);
    }

    return mav;
  }
}
