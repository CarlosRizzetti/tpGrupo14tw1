package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorProducto {

  private static final Logger LOGGER = Logger.getLogger(ControladorProducto.class.getName());
  private final ServicioProducto servicioProducto;
  private final ServicioCategoria servicioCategoria;
  private final ServicioTimer servicioTimer;
  private final ServicioReglaVencimiento servicioReglaVencimiento;
  private static final String CATEGORIA = "categoria";

  @Autowired
  public ControladorProducto(
    ServicioProducto servicioProducto,
    ServicioCategoria servicioCategoria,
    ServicioTimer servicioTimer,
    ServicioReglaVencimiento servicioReglaVencimiento
  ) {
    this.servicioProducto = servicioProducto;
    this.servicioCategoria = servicioCategoria;
    this.servicioTimer = servicioTimer;
    this.servicioReglaVencimiento = servicioReglaVencimiento;
  }

  // GET — mostrar el formulario
  @RequestMapping(value = "/producto/nuevo", method = RequestMethod.GET)
  public ModelAndView mostrarFormulario(HttpSession session) {
    if (!esAdministrador(session)) {
      return new ModelAndView("redirect:/acceso-denegado");
    }
    ModelAndView mav = new ModelAndView("producto/nuevo");
    List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();
    mav.addObject("categorias", categorias);
    mav.addObject("datosProducto", new ProductoDto());
    return mav;
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
      if (LOGGER.isLoggable(java.util.logging.Level.SEVERE)) {
        LOGGER.severe(">>> ERROR: " + e.getClass().getName() + " - " + e.getMessage());
      }
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

  @RequestMapping(path = "/category/{id}/products", method = RequestMethod.GET)
  public ModelAndView mostrarProductosPorCategoria(@PathVariable Long id, HttpSession session) {
    ModelMap modelo = new ModelMap();
    CategoriaDto categoria = servicioCategoria.obtenerCategoriaPorId(id);
    session.setAttribute(CATEGORIA, categoria);
    List<Producto> productos = servicioProducto.obtenerProductosPorCategoria(id);

    modelo.put(CATEGORIA, categoria);
    modelo.put("productos", productos);
    return new ModelAndView("productos", modelo);
  }

  @RequestMapping(path = "/product/{id}", method = RequestMethod.GET)
  public ModelAndView mostrarVencimientoProducto(@PathVariable Long id, HttpSession session) {
    ModelMap modelo = new ModelMap();
    Producto producto = servicioProducto.obtenerProductoConReglas(id);
    Set<ReglaVencimiento> reglas = producto.getReglas();
    CategoriaDto categoriaDto = (CategoriaDto) session.getAttribute(CATEGORIA);

    modelo.put("producto", producto);
    modelo.put("reglas", reglas);
    modelo.put(CATEGORIA, categoriaDto);

    return new ModelAndView("producto-vencimiento", modelo);
  }

  @RequestMapping(path = "/producto/{id}/generar", method = RequestMethod.POST)
  public String imprimirConstancia(
    @PathVariable Long id,
    @RequestParam("offset_minutes") Integer offsetMinutes,
    @RequestParam(name = "categoryId", required = false) Long categoryId,
    @RequestParam(name = "reglaId") Long reglaId
  ) {
    Producto producto = servicioProducto.obtenerProductoConReglas(id);
    Categoria categoria = determinarCategoria(producto, categoryId);
    servicioReglaVencimiento.generarVencimiento(producto, categoria, reglaId, offsetMinutes);

    return "redirect:/dashboard";
  }

  private Categoria determinarCategoria(Producto producto, Long categoryId) {
    if (producto.getCategorias().isEmpty()) {
      return null;
    }
    if (categoryId != null) {
      for (Categoria c : producto.getCategorias()) {
        if (c.getId().equals(categoryId)) {
          return c;
        }
      }
    }
    return producto.getCategorias().stream().findFirst().orElse(null);
  }

  private boolean esAdministrador(HttpSession session) {
    Object rol = session.getAttribute("ROL");
    if (rol == null) return false;
    return "ADMIN".equalsIgnoreCase(rol.toString());
  }
}
