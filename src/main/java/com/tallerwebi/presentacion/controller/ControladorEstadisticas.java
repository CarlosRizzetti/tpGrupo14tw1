package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioEstadistica;
import com.tallerwebi.presentacion.dto.EstadisticasDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controlador de la sección de estadísticas.
 * Solo delega en {@link ServicioEstadistica}; no accede a repositorios.
 */
@Controller
public class ControladorEstadisticas {

  private static final Logger logger = LoggerFactory.getLogger(ControladorEstadisticas.class);
  private static final int DIAS_POR_DEFECTO = 30;

  private final ServicioEstadistica servicioEstadistica;

  @Autowired
  public ControladorEstadisticas(ServicioEstadistica servicioEstadistica) {
    this.servicioEstadistica = servicioEstadistica;
  }

  @GetMapping("/estadisticas")
  public ModelAndView index() {
    ModelAndView mav = new ModelAndView("estadisticas/estadisticas");
    mav.addObject("dias", DIAS_POR_DEFECTO);
    return mav;
  }

  @GetMapping("/estadisticas/datos")
  @ResponseBody
  public ResponseEntity<EstadisticasDTO> obtenerDatos(
    @RequestParam(name = "dias", defaultValue = "30") int dias
  ) {
    try {
      EstadisticasDTO estadisticas = servicioEstadistica.obtenerEstadisticas(dias);
      return ResponseEntity.ok(estadisticas);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      logger.error("Error al obtener las estadísticas", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
