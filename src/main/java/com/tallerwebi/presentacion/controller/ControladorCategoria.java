package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Usuario;
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

      boolean isAdmin = authentication
        .getAuthorities()
        .stream()
        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

      if (isAdmin) {
        categorias = this.servicioCategoria.obtenerLasCategoriasParaElMenu();
      } else {
        categorias = this.servicioCategoria.obtenerCategoriasPorUsuario(authentication.getName());
      }
    } else {
      categorias = new ArrayList<>();
    }

    if (categorias == null || categorias.isEmpty()) {
      mav.addObject("mensajeVacio", "Todavía no te asignaron una categoría");
    }

    mav.addObject("categorias", categorias);
    return mav;
  }
}
