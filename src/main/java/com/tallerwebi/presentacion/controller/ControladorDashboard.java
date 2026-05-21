package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioDashboard;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @DeleteMapping("/active-timers/{timerId}/{categoryId}")
  public ResponseEntity<String> eliminarTimer(
    @PathVariable Long timerId,
    @PathVariable Long categoryId
  ) {
    try {
      servicioDashboard.eliminarTimer(timerId);
      return ResponseEntity.ok("Timer eliminado correctamente");
    } catch (IllegalArgumentException e) {
      return ResponseEntity
        .status(org.springframework.http.HttpStatus.BAD_REQUEST)
        .body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity
        .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error al eliminar el timer");
    }
  }
}
