package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorProducto {

  private final ServicioProducto servicioProducto;
  private final ServicioCategoria servicioCategoria;

  @Autowired
  public ControladorProducto(
    ServicioProducto servicioProducto,
    ServicioCategoria servicioCategoria
  ) {
    this.servicioProducto = servicioProducto;
    this.servicioCategoria = servicioCategoria;
  }

  // GET — mostrar el formulario
  @RequestMapping(value = "/producto/nuevo", method = RequestMethod.GET)
  public ModelAndView mostrarFormulario(HttpSession session) {
    if (!esAdministrador(session)) {
      return new ModelAndView("redirect:/acceso-denegado");
    }

    ModelMap modelo = new ModelMap();
    List<DatosCategoria> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();
    modelo.put("categorias", categorias);
    modelo.put("datosProducto", new DatosProducto());
    return new ModelAndView("producto/nuevo", modelo);
  }

  // POST — procesar el formulario
  @RequestMapping(value = "/producto/nuevo", method = RequestMethod.POST)
  public ModelAndView crearProducto(
    @ModelAttribute DatosProducto datosProducto,
    HttpSession session
  ) {
    if (!esAdministrador(session)) {
      return new ModelAndView("redirect:/acceso-denegado");
    }

    try {
      servicioProducto.crearProducto(datosProducto);
      return new ModelAndView("redirect:/producto/exito");
    } catch (IllegalArgumentException e) {
      ModelMap modelo = new ModelMap();
      List<DatosCategoria> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();
      modelo.put("categorias", categorias);
      modelo.put("datosProducto", datosProducto);
      modelo.put("error", e.getMessage());
      return new ModelAndView("producto/nuevo", modelo);
    }
  }

  @RequestMapping("/producto/exito")
  public ModelAndView exito() {
    return new ModelAndView("producto/exito");
  }

  // Verificar que el usuario en sesión sea Admin
  private boolean esAdministrador(HttpSession session) {
    Object usuario = session.getAttribute("usuario");
    if (usuario == null) return false;
    com.tallerwebi.dominio.Usuario user = (com.tallerwebi.dominio.Usuario) usuario;
    return "ADMIN".equalsIgnoreCase(user.getRol());
  }
}
