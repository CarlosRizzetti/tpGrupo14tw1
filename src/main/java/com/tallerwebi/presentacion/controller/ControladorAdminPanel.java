package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdminPanel {

  private final ServicioArticulo servicioArticulo;

  @Autowired
  public ControladorAdminPanel(ServicioArticulo servicioArticulo) {
    this.servicioArticulo = servicioArticulo;
  }

  @RequestMapping(path = "/admin", method = RequestMethod.GET)
  public ModelAndView panelDeControl(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return new ModelAndView("redirect:/acceso-denegado");
    }

    boolean isAdmin = authentication
      .getAuthorities()
      .stream()
      .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    if (!isAdmin) {
      return new ModelAndView("redirect:/acceso-denegado");
    }

    ModelMap model = new ModelMap();
    model.put("email", authentication.getName());
    model.put("notificaciones", servicioArticulo.obtenerNotificacionesVencimiento());

    return new ModelAndView("funcionalidadesAdmin/panel", model);
  }

  @RequestMapping(path = "/acceso-denegado", method = RequestMethod.GET)
  public ModelAndView accesoDenegado() {
    return new ModelAndView("acceso-denegado");
  }
}
