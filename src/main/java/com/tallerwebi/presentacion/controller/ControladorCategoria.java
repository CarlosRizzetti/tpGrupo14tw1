package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorCategoria {

  public ServicioCategoria servicioCategoria;

  @Autowired
  public ControladorCategoria(ServicioCategoria servicioCategoria) {
    this.servicioCategoria = servicioCategoria;
  }

  @RequestMapping("/home")
  public ModelAndView index(Authentication authentication) {
    ModelAndView mav = new ModelAndView("home");
    List<CategoriaDto> categorias;

    if (authentication != null && authentication.isAuthenticated()) {
      String email = authentication.getName();
      mav.addObject("userEmail", email);

      categorias = this.servicioCategoria.obtenerLasCategoriasParaElMenu();
    } else {
      categorias = new ArrayList<>();
    }

    if (categorias == null || categorias.isEmpty()) {
      mav.addObject("mensajeVacio", "No hay categorías disponibles");
    }

    mav.addObject("categorias", categorias);
    return mav;
  }
}
