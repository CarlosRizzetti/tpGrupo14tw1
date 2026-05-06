package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorCategoria {

  public ServicioCategoria servicioCategoria;

  @Autowired
  public ControladorCategoria(ServicioCategoria servicioCategoria) {
    this.servicioCategoria = servicioCategoria;
  }

  @RequestMapping("/")
  public ModelAndView index() {
    ModelMap modelo = new ModelMap();
    List<DatosCategoria> categorias = this.servicioCategoria.obtenerLasCategoriasParaElMenu();
    modelo.put("categorias", categorias);
    return new ModelAndView("home", modelo);
  }
}
