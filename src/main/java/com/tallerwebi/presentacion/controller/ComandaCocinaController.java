package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.dominio.interfaces.RepositorioComanda;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/cocina")
public class ComandaCocinaController {

  private final RepositorioComanda repositorioComanda;
  private final ServicioComanda servicioComanda;

  @Autowired
  public ComandaCocinaController(
    RepositorioComanda repositorioComanda,
    ServicioComanda servicioComanda
  ) {
    this.repositorioComanda = repositorioComanda;
    this.servicioComanda = servicioComanda;
  }

  // Pantalla principal de cocina: comandas esperando ser preparadas
  @RequestMapping(path = "/comandas", method = RequestMethod.GET)
  public ModelAndView listarComandasPendientes() {
    ModelAndView modelAndView = new ModelAndView("comandas-pendientes");
    modelAndView.addObject("comandas", repositorioComanda.listarPendientes());
    return modelAndView;
  }

  // El cocinero marca la comanda como lista: acá corre toda la validación de timers
  @RequestMapping(path = "/comandas/{id}/sacar", method = RequestMethod.POST)
  public ModelAndView sacarComanda(@PathVariable Long id) {
    try {
      servicioComanda.sacarComanda(id);

      ModelAndView modelAndView = new ModelAndView("redirect:/cocina/comandas");
      return modelAndView;
    } catch (IngredientesNoDisponiblesException e) {
      ModelAndView modelAndView = new ModelAndView("comandas-pendientes");
      modelAndView.addObject("comandas", repositorioComanda.listarPendientes());
      modelAndView.addObject("error", e.getMessage());
      modelAndView.addObject("comandaConError", id);
      return modelAndView;
    }
  }
}
