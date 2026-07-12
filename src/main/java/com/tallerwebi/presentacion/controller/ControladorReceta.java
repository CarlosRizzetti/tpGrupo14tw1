package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Receta;
import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioReceta;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorReceta {

  private final ServicioReceta servicioReceta;
  private final ServicioProducto servicioProducto;
  private final ServicioArticulo servicioArticulo;

  @Autowired
  public ControladorReceta(
    ServicioReceta servicioReceta,
    ServicioProducto servicioProducto,
    ServicioArticulo servicioArticulo
  ) {
    this.servicioReceta = servicioReceta;
    this.servicioProducto = servicioProducto;
    this.servicioArticulo = servicioArticulo;
  }

  @GetMapping("/admin/recetas")
  public ModelAndView listarRecetas() {
    List<Receta> recetas = servicioReceta.obtenerTodas();
    ModelMap modelo = new ModelMap();
    modelo.put("recetas", recetas);
    return new ModelAndView("recetas/lista", modelo);
  }

  @GetMapping("/admin/recetas/nueva")
  public ModelAndView formularioNuevaReceta() {
    List<Producto> productos = servicioProducto.obtenerTodosLosProductos();
    List<Articulos> articulos = servicioArticulo.obtenerTodosLosArticulos();

    ModelMap modelo = new ModelMap();
    modelo.put("productos", productos);
    modelo.put("articulos", articulos);
    return new ModelAndView("recetas/formulario", modelo);
  }

  @PostMapping("/admin/recetas/guardar")
  public ModelAndView guardarReceta(
    @RequestParam("productoId") Long productoId,
    @RequestParam(value = "articulosIds", required = false) List<Long> articulosIds,
    @RequestParam(value = "cantidades", required = false) List<Double> cantidades
  ) {
    Producto producto = servicioProducto.obtenerProductoPorId(productoId);

    if (producto != null) {
      servicioReceta.guardarReceta(producto, articulosIds, cantidades);
    }

    return new ModelAndView("redirect:/admin/recetas");
  }
}
