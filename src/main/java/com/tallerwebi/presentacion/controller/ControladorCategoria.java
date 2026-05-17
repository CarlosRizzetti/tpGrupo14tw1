package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
  public ModelAndView index(HttpSession session) {
    ModelAndView mav = new ModelAndView("home");
    List<CategoriaDto> categorias = this.servicioCategoria.obtenerLasCategoriasParaElMenu();
    mav.addObject("categorias", categorias);
    session.setAttribute("categorias", categorias);
    CategoriaDto categoria = new CategoriaDto(new Categoria("asd", true, "mccafe"));
    session.setAttribute("categoria", categoria);
    return mav;
  }
}
