package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTimer {

  private final ServicioTimer servicioTimer;
  private final ServicioCategoria servicioCategoria;

  private final ServicioLote servicioLote;

  public ControladorTimer(
    ServicioTimer servicioTimer,
    ServicioCategoria servicioCategoria,
    ServicioLote servicioLote
  ) {
    this.servicioTimer = servicioTimer;
    this.servicioCategoria = servicioCategoria;
    this.servicioLote = servicioLote;
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

  @RequestMapping(path = "/admin/timers/{id}", method = RequestMethod.GET)
  public ModelAndView mostrarDetalleTimer(@PathVariable Long id) {
    ModelMap modelo = new ModelMap();

    TimerDTO timerDTO = servicioTimer.buscarPorIdDTO(id);

    modelo.put("timer", timerDTO);
    return new ModelAndView("funcionalidadesAdmin/timer/detalleTimers", modelo);
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
