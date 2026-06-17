package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdmin {

  private final ServicioAdmin servicioAdmin;

  @Autowired
  public ControladorAdmin(ServicioAdmin servicioAdmin) {
    this.servicioAdmin = servicioAdmin;
  }

  @RequestMapping(value = "/admin/usuarios/{id}/aprobar", method = RequestMethod.POST)
  public ModelAndView aprobarUsuario(@PathVariable Long id, @RequestParam Long categoriaId) {
    servicioAdmin.aprobarUsuario(id, categoriaId);

    return new ModelAndView("redirect:/admin/usuarios");
  }
}
