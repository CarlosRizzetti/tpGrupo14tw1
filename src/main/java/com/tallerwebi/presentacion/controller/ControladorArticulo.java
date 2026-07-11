package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorArticulo {

  private static final String ARTICULOS = "articulos";
  private final ServicioArticulo servicioArticulo;

  @Autowired
  public ControladorArticulo(ServicioArticulo servicioArticulo) {
    this.servicioArticulo = servicioArticulo;
  }

  @RequestMapping(path = "/admin/articulos", method = RequestMethod.GET)
  public ModelAndView mostrarArticulosDelAlmacen() {
    ModelMap modelo = new ModelMap();
    List<Articulos> articulos = servicioArticulo.obtenerTodosLosArticulos();
    List<com.tallerwebi.presentacion.dto.StockArticuloDto> stockArticulos =
      servicioArticulo.obtenerStockAgrupado();
    modelo.put(ARTICULOS, articulos);
    modelo.put("stockArticulos", stockArticulos);
    return new ModelAndView(ARTICULOS, modelo);
  }

  @RequestMapping(path = "/admin/stock", method = RequestMethod.GET)
  public ModelAndView mostrarStock() {
    ModelMap modelo = new ModelMap();
    List<com.tallerwebi.presentacion.dto.StockArticuloDto> stockArticulos =
      servicioArticulo.obtenerStockAgrupado();
    modelo.put("stockArticulos", stockArticulos);
    return new ModelAndView("stock", modelo);
  }

  @RequestMapping(
    path = { "/admin/nuevo-articulo", "/trazabilidad/nuevo-articulo" },
    method = RequestMethod.GET
  )
  public ModelAndView mostrarFormularioNuevoArticulo() {
    ModelMap modelo = new ModelMap();
    modelo.put("articulo", new Articulos());
    return new ModelAndView("nuevo-articulo", modelo);
  }

  @RequestMapping(
    path = { "/admin/nuevo-articulo", "/trazabilidad/nuevo-articulo" },
    method = RequestMethod.POST
  )
  public ModelAndView registrarArticulo(
    @org.springframework.web.bind.annotation.ModelAttribute("articulo") Articulos articulo,
    @org.springframework.web.bind.annotation.RequestParam("fechaIngresoStr") String fechaIngresoStr,
    @org.springframework.web.bind.annotation.RequestParam(
      "fechaVencimientoStr"
    ) String fechaVencimientoStr
  ) {
    try {
      if (fechaIngresoStr != null && !fechaIngresoStr.isEmpty()) {
        articulo.setFechaDeIngreso(LocalDateTime.parse(fechaIngresoStr).atOffset(ZoneOffset.UTC));
      }
      if (fechaVencimientoStr != null && !fechaVencimientoStr.isEmpty()) {
        articulo.setFechaDeVencimiento(
          LocalDateTime.parse(fechaVencimientoStr).atOffset(ZoneOffset.UTC)
        );
      }
      servicioArticulo.registrarArticulo(articulo);
      return new ModelAndView("redirect:/admin");
    } catch (Exception e) {
      ModelMap modelo = new ModelMap();
      modelo.put("articulo", articulo);
      modelo.put("error", "Error al registrar el articulo: " + e.getMessage());
      return new ModelAndView("nuevo-articulo", modelo);
    }
  }

  @RequestMapping(path = "/admin/articulos/descontar", method = RequestMethod.GET)
  public ModelAndView mostrarFormularioDescontarStock() {
    ModelMap modelo = new ModelMap();
    List<Articulos> articulos = servicioArticulo.obtenerTodosLosArticulos();
    modelo.put(ARTICULOS, articulos);
    return new ModelAndView("funcionalidadesAdmin/articulos-descontar", modelo);
  }

  @RequestMapping(path = "/admin/articulos/descontar", method = RequestMethod.POST)
  public ModelAndView descontarStock(
    @org.springframework.web.bind.annotation.RequestParam("id") Long id,
    @org.springframework.web.bind.annotation.RequestParam("cantidad") Double cantidad
  ) {
    try {
      servicioArticulo.descontarStock(id, cantidad);
      return new ModelAndView("redirect:/admin/articulos/descontar?exito=true");
    } catch (Exception e) {
      ModelMap modelo = new ModelMap();
      List<Articulos> articulos = servicioArticulo.obtenerTodosLosArticulos();
      modelo.put(ARTICULOS, articulos);
      modelo.put("error", e.getMessage());
      return new ModelAndView("funcionalidadesAdmin/articulos-descontar", modelo);
    }
  }
}
