package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioProductoFinal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorReceta {

  private final ServicioProductoFinal servicioProductoFinal;
  private final ServicioCategoria servicioCategoria;
  private final ServicioProducto servicioProducto;

  @Autowired
  public ControladorReceta(
    ServicioProductoFinal servicioProductoFinal,
    ServicioCategoria servicioCategoria,
    ServicioProducto servicioProducto
  ) {
    this.servicioProductoFinal = servicioProductoFinal;
    this.servicioCategoria = servicioCategoria;
    this.servicioProducto = servicioProducto;
  }

  @RequestMapping(path = "/admin/recetas", method = RequestMethod.GET)
  public ModelAndView mostrarRecetas() {
    ModelMap modelo = new ModelMap();
    modelo.put("productoFinal", new ProductoFinal());
    modelo.put("categorias", servicioCategoria.obtenerLasCategoriasParaElMenu());
    modelo.put("productos", servicioProducto.obtenerTodosLosProductos());
    modelo.put("productosFinales", servicioProductoFinal.listarTodos());
    return new ModelAndView("funcionalidadesAdmin/recetas", modelo);
  }

  @RequestMapping(path = "/admin/recetas/nueva", method = RequestMethod.POST)
  public ModelAndView guardarReceta(
    @ModelAttribute("productoFinal") ProductoFinal productoFinal,
    @RequestParam("categoriaId") Long categoriaId,
    @RequestParam(value = "ingredientesIds", required = false) List<Long> ingredientesIds,
    @RequestParam(value = "cantidades", required = false) List<Integer> cantidades
  ) {
    try {
      servicioProductoFinal.guardarProductoFinal(
        productoFinal,
        categoriaId,
        ingredientesIds,
        cantidades
      );
      return new ModelAndView("redirect:/admin/recetas?exito");
    } catch (Exception e) {
      ModelMap modelo = new ModelMap();
      modelo.put("productoFinal", productoFinal);
      modelo.put("categorias", servicioCategoria.obtenerLasCategoriasParaElMenu());
      modelo.put("productos", servicioProducto.obtenerTodosLosProductos());
      modelo.put("productosFinales", servicioProductoFinal.listarTodos());
      modelo.put("error", "Error al crear la composición: " + e.getMessage());
      return new ModelAndView("funcionalidadesAdmin/recetas", modelo);
    }
  }
}
