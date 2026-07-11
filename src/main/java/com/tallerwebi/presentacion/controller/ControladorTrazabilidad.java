package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Trazabilidad;
import com.tallerwebi.dominio.interfaces.ServicioProduccion;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTrazabilidad {

  private final ServicioProduccion servicioProduccion;

  @Autowired
  public ControladorTrazabilidad(ServicioProduccion servicioProduccion) {
    this.servicioProduccion = servicioProduccion;
  }

  @GetMapping("/admin/trazabilidad")
  public ModelAndView verTrazabilidad() {
    List<Trazabilidad> historial = servicioProduccion.obtenerTrazabilidadCompleta();
    ModelMap modelo = new ModelMap();
    modelo.put("historial", historial);
    return new ModelAndView("trazabilidad/historial", modelo);
  }
}
