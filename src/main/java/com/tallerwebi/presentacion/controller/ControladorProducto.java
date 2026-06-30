package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorProducto {

  private static final Logger LOGGER = Logger.getLogger(ControladorProducto.class.getName());
  private final ServicioProducto servicioProducto;
  private final ServicioCategoria servicioCategoria;
  private final ServicioReglaVencimiento servicioReglaVencimiento;
  private static final String CATEGORIA = "categoria";
  private final ServicioUsuario servicioUsuario;

  @Autowired
  public ControladorProducto(
    ServicioProducto servicioProducto,
    ServicioCategoria servicioCategoria,
    ServicioReglaVencimiento servicioReglaVencimiento,
    ServicioUsuario servicioUsuario
  ) {
    this.servicioProducto = servicioProducto;
    this.servicioCategoria = servicioCategoria;
    this.servicioReglaVencimiento = servicioReglaVencimiento;
    this.servicioUsuario = servicioUsuario;
  }

  // GET — mostrar el formulario
  @RequestMapping(value = "/admin/producto/nuevo", method = RequestMethod.GET)
  public ModelAndView mostrarFormulario() {
    ModelAndView mav = new ModelAndView("funcionalidadesAdmin/producto/nuevo");
    List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();
    mav.addObject("categorias", categorias);
    mav.addObject("datosProducto", new ProductoDto());
    return mav;
  }

  // POST — procesar el formulario
  @RequestMapping(value = "admin/producto/nuevo", method = RequestMethod.POST)
  public ModelAndView crearProducto(@ModelAttribute ProductoDto productoDto) {
    try {
      servicioProducto.crearProducto(productoDto);
      return new ModelAndView("redirect:/admin/producto/exito");
    } catch (IllegalArgumentException e) {
      if (LOGGER.isLoggable(java.util.logging.Level.SEVERE)) {
        LOGGER.severe(">>> ERROR: " + e.getClass().getName() + " - " + e.getMessage());
      }
      ModelMap modelo = new ModelMap();
      List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();
      modelo.put("categorias", categorias);
      modelo.put("datosProducto", productoDto);
      modelo.put("error", e.getMessage());
      return new ModelAndView("funcionalidadesAdmin/producto/nuevo", modelo);
    }
  }

  @RequestMapping("/admin/producto/exito")
  public ModelAndView exito() {
    return new ModelAndView("funcionalidadesAdmin/producto/exito");
  }

  @RequestMapping(value = "/admin/productos", method = RequestMethod.GET)
  public ModelAndView gestionProductos(
    @RequestParam(name = "categoriaId", required = false) Long categoriaId
  ) {
    ModelMap modelo = new ModelMap();
    modelo.put("productos", servicioProducto.listarProductos(categoriaId));
    modelo.put("categorias", servicioCategoria.obtenerLasCategoriasParaElMenu());
    modelo.put("categoriaSeleccionada", categoriaId);
    return new ModelAndView("funcionalidadesAdmin/producto/gestion", modelo);
  }

  @RequestMapping(value = "/admin/productos/{id}/agregar-stock", method = RequestMethod.POST)
  public String agregarStock(
    @PathVariable Long id,
    @RequestParam("cantidad") Integer cantidad,
    @RequestParam(name = "categoriaId", required = false) Long categoriaId
  ) {
    servicioProducto.agregarStock(id, cantidad);
    if (categoriaId != null) {
      return "redirect:/admin/productos?categoriaId=" + categoriaId;
    }
    return "redirect:/admin/productos";
  }

  @RequestMapping(value = "/admin/productos/{id}/quitar-stock", method = RequestMethod.POST)
  public String quitarStock(
    @PathVariable Long id,
    @RequestParam("cantidad") Integer cantidad,
    @RequestParam(name = "categoriaId", required = false) Long categoriaId
  ) {
    servicioProducto.quitarStock(id, cantidad);
    if (categoriaId != null) {
      return "redirect:/admin/productos?categoriaId=" + categoriaId;
    }
    return "redirect:/admin/productos";
  }

  @RequestMapping(path = "/category/{id}/products", method = RequestMethod.GET)
  public ModelAndView mostrarProductosPorCategoria(
    @PathVariable Long id,
    HttpSession session,
    org.springframework.security.core.Authentication authentication
  ) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return new ModelAndView("redirect:/login");
    }

    boolean isAdmin = authentication
      .getAuthorities()
      .stream()
      .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

    if (!isAdmin) {
      List<CategoriaDto> categoriasUsuario = servicioCategoria.obtenerCategoriasPorUsuario(
        authentication.getName()
      );
      boolean tienePermiso = categoriasUsuario.stream().anyMatch(c -> c.getId().equals(id));
      if (!tienePermiso) {
        return new ModelAndView("redirect:/home");
      }
    }

    ModelMap modelo = new ModelMap();
    CategoriaDto categoria = servicioCategoria.obtenerCategoriaPorId(id);
    session.setAttribute(CATEGORIA, categoria);
    List<Producto> productos = servicioProducto.obtenerProductosPorCategoria(id);

    // Obtener los usuarios de la categoría para mostrarlos en la vista
    List<com.tallerwebi.dominio.entity.Usuario> usuarios =
      servicioCategoria.obtenerUsuariosPorCategoria(id);

    modelo.put(CATEGORIA, categoria);
    modelo.put("productos", productos);
    modelo.put("usuarios", usuarios);
    return new ModelAndView("listadoDeProductosYReglas/productos", modelo);
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

    return new ModelAndView("listadoDeProductosYReglas/producto-vencimiento", modelo);
  }

  @RequestMapping(path = "/producto/{id}/generar", method = RequestMethod.POST)
  public String imprimirConstancia(
    @PathVariable Long id,
    @RequestParam("offset_minutes") Integer offsetMinutes,
    @RequestParam(name = "categoryId", required = false) Long categoryId,
    @RequestParam(name = "reglaId") Long reglaId,
    @RequestParam(name = "cantidad") Integer cantidad,
    @AuthenticationPrincipal User usuarioLogueado
  ) {
    Producto producto = servicioProducto.obtenerProductoConReglas(id);
    Categoria categoria = determinarCategoria(producto, categoryId);
    Usuario usuario = servicioUsuario.obtenerUsuarioPorEmail(usuarioLogueado.getUsername());
    servicioReglaVencimiento.generarVencimiento(
      producto,
      categoria,
      reglaId,
      offsetMinutes,
      cantidad,
      usuario
    );

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
}
