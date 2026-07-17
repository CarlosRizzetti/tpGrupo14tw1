package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.SinStockSuficienteException;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import com.tallerwebi.dominio.utils.AuthenticationUtils;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.ProductoDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorProducto {

  private static final Logger LOGGER = Logger.getLogger(ControladorProducto.class.getName());
  private final ServicioProducto servicioProducto;
  private final ServicioCategoria servicioCategoria;
  private final ServicioReglaVencimiento servicioReglaVencimiento;
  private static final String CATEGORIA = "categoria";
  private final ServicioUsuario servicioUsuario;
  private final ServicioLote servicioLote;

  @Autowired
  public ControladorProducto(
    ServicioProducto servicioProducto,
    ServicioCategoria servicioCategoria,
    ServicioReglaVencimiento servicioReglaVencimiento,
    ServicioUsuario servicioUsuario,
    ServicioLote servicioLote
  ) {
    this.servicioProducto = servicioProducto;
    this.servicioCategoria = servicioCategoria;
    this.servicioReglaVencimiento = servicioReglaVencimiento;
    this.servicioUsuario = servicioUsuario;
    this.servicioLote = servicioLote;
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
    List<Producto> productos = servicioProducto.listarProductos(categoriaId);
    Map<Long, Integer> stockPorProducto = productos
      .stream()
      .collect(Collectors.toMap(Producto::getId, servicioLote::stockDisponibleDe));
    modelo.put("productos", productos);
    modelo.put("stockPorProducto", stockPorProducto);
    modelo.put("categorias", servicioCategoria.obtenerLasCategoriasParaElMenu());
    modelo.put("categoriaSeleccionada", categoriaId);
    return new ModelAndView("funcionalidadesAdmin/producto/gestion", modelo);
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

    String email = authentication.getName();
    boolean isAdmin = authentication
      .getAuthorities()
      .stream()
      .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    boolean tienePermiso = servicioCategoria
      .obtenerCategoriasPorUsuario(email)
      .stream()
      .anyMatch(c -> c.getId().equals(id));

    if (!isAdmin && !tienePermiso) {
      CategoriaDto categoria = servicioCategoria.obtenerCategoriaPorId(id);
      session.setAttribute(CATEGORIA, categoria);
      return new ModelAndView("redirect:/dashboard");
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
  public ModelAndView mostrarVencimientoProducto(
    @PathVariable Long id,
    HttpSession session,
    Authentication authentication
  ) {
    ModelMap modelo = new ModelMap();
    Producto producto = servicioProducto.obtenerProductoConReglas(id);
    Set<ReglaVencimiento> reglas = producto.getReglas();
    CategoriaDto categoriaDto = (CategoriaDto) session.getAttribute(CATEGORIA);
    Integer stockDisponible = servicioLote.stockDisponibleDe(producto);

    boolean isAdmin = authentication
      .getAuthorities()
      .stream()
      .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    boolean tienePermiso =
      categoriaDto != null &&
      servicioCategoria
        .obtenerCategoriasPorUsuario(authentication.getName())
        .stream()
        .anyMatch(c -> c.getId().equals(categoriaDto.getId()));

    modelo.put("puedeGenerarTimer", isAdmin || tienePermiso);
    modelo.put("producto", producto);
    modelo.put("reglas", reglas);
    modelo.put(CATEGORIA, categoriaDto);
    modelo.put("stockDisponible", stockDisponible);

    return new ModelAndView("listadoDeProductosYReglas/producto-vencimiento", modelo);
  }

  @RequestMapping(path = "/producto/{id}/generar", method = RequestMethod.POST)
  public String imprimirConstancia(
    @PathVariable Long id,
    @RequestParam("offset_minutes") Integer offsetMinutes,
    @RequestParam(name = "categoryId", required = false) Long categoryId,
    @RequestParam(name = "reglaId") Long reglaId,
    @RequestParam(name = "cantidad") Integer cantidad,
    Authentication authentication,
    RedirectAttributes redirectAttributes
  ) {
    final String email = AuthenticationUtils.obtenerEmailDeAutenticacion(authentication);
    Producto producto = servicioProducto.obtenerProductoConReglas(id);
    Categoria categoria = determinarCategoria(producto, categoryId);

    boolean isAdmin = authentication
      .getAuthorities()
      .stream()
      .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    boolean tienePermiso =
      categoria != null &&
      servicioCategoria
        .obtenerCategoriasPorUsuario(email)
        .stream()
        .anyMatch(c -> c.getId().equals(categoria.getId()));

    if (!isAdmin && !tienePermiso) {
      redirectAttributes.addFlashAttribute(
        "error",
        "No tienes permisos para generar timers en esta categoría"
      );
      return "redirect:/product/" + id;
    }

    Usuario usuario = servicioUsuario.obtenerUsuarioPorEmail(email);

    try {
      servicioReglaVencimiento.generarVencimiento(
        producto,
        categoria,
        reglaId,
        offsetMinutes,
        cantidad,
        usuario
      );
    } catch (SinStockSuficienteException | IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/product/" + id;
    }

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
