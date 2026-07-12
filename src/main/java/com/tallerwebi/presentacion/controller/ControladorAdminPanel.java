package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioLote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdminPanel {

  private final ServicioLote servicioLote;

  @Autowired
  public ControladorAdminPanel(ServicioLote servicioLote) {
    this.servicioLote = servicioLote;
  }

  @RequestMapping(path = "/admin", method = RequestMethod.GET)
  public ModelAndView panelDeControl(Authentication authentication) {
    ModelMap model = new ModelMap();
    model.put("email", authentication.getName());
    model.put("notificaciones", servicioLote.obtenerNotificacionesVencimiento());

    return new ModelAndView("funcionalidadesAdmin/panel", model);
  }
}
