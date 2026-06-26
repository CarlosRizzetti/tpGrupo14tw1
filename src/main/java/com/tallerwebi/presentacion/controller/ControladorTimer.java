package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTimer {

  private final ServicioTimer servicioTimer;
  private final ServicioCategoria servicioCategoria;

  public ControladorTimer(ServicioTimer servicioTimer, ServicioCategoria servicioCategoria) {
    this.servicioTimer = servicioTimer;
    this.servicioCategoria = servicioCategoria;
  }

  @GetMapping("/admin/timers/historial")
  public ModelAndView verHistorialDeTimers() {
    List<TimerDTO> timers = servicioTimer.obtenerTodosLosTimers();
    List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();
    ModelMap modelo = new ModelMap();
    modelo.put("timers", timers);
    modelo.put("categorias", categorias);
    return new ModelAndView("funcionalidadesAdmin/timer/historialTimers", modelo);
  }

  @GetMapping("/timers/obtener/")
  public ResponseEntity<?> obtenerTimersConFiltro(
    @RequestParam(required = false) EstadoTimer estado,
    @RequestParam(required = false) Long categoriaId
  ) {
    List<TimerDTO> timers = servicioTimer.obtenerTimersConFiltro(estado, categoriaId);
    return ResponseEntity.ok(Map.of("timers", timers));
  }
}
