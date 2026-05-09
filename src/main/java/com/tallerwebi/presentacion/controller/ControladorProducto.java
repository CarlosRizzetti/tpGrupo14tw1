package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
    List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();
    modelo.put("categorias", categorias);
    modelo.put("datosProducto", new ProductoDto());
    return new ModelAndView("producto/nuevo", modelo);
  }

  // POST — procesar el formulario
  @RequestMapping(value = "/producto/nuevo", method = RequestMethod.POST)
  public ModelAndView crearProducto(@ModelAttribute ProductoDto productoDto, HttpSession session) {
    if (!esAdministrador(session)) {
      return new ModelAndView("redirect:/acceso-denegado");
    }

    try {
      servicioProducto.crearProducto(productoDto);
      return new ModelAndView("redirect:/producto/exito");
    } catch (IllegalArgumentException e) {
      ModelMap modelo = new ModelMap();
      List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();
      modelo.put("categorias", categorias);
      modelo.put("datosProducto", productoDto);
      modelo.put("error", e.getMessage());
      return new ModelAndView("producto/nuevo", modelo);
    }
  }

  @RequestMapping("/producto/exito")
  public ModelAndView exito() {
    return new ModelAndView("producto/exito");
  }

  @RequestMapping("/category/{id}/products")
  public ModelAndView mostrarProductosPorCategoria(@PathVariable Long id) {
    List<Producto> productos = servicioProducto.obtenerProductosPorCategoria(id);
    ModelMap modelo = new ModelMap();
    modelo.put("productos", productos);
    return new ModelAndView("productos", modelo);
  }

  // Verificar que el usuario en sesión sea Admin
  private boolean esAdministrador(HttpSession session) {
    Object usuario = session.getAttribute("usuario");
    if (usuario == null) return false;
    com.tallerwebi.dominio.entity.Usuario user = (com.tallerwebi.dominio.entity.Usuario) usuario;
    return "ADMIN".equalsIgnoreCase(user.getRol());
  }
}
