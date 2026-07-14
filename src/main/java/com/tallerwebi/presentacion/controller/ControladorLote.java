package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.presentacion.dto.StockProductoDTO;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLote {

  private static final String ARTICULOS = "articulos";
  private static final String PRODUCTOS = "productos";
  private static final String VISTA_NUEVO_LOTE = "nuevo-articulo";

  private final ServicioLote servicioLote;
  private final ServicioProducto servicioProducto;

  @Autowired
  public ControladorLote(ServicioLote servicioLote, ServicioProducto servicioProducto) {
    this.servicioLote = servicioLote;
    this.servicioProducto = servicioProducto;
  }

  @RequestMapping(path = "/admin/articulos", method = RequestMethod.GET)
  public ModelAndView mostrarLotesDelAlmacen() {
    ModelMap modelo = new ModelMap();
    List<Lote> lotes = servicioLote.obtenerTodosLosLotes();
    List<StockProductoDTO> stockLotes = servicioLote.obtenerStockAgrupado();
    modelo.put(ARTICULOS, lotes);
    modelo.put("stockArticulos", stockLotes);
    return new ModelAndView(ARTICULOS, modelo);
  }

  @RequestMapping(path = "/admin/stock", method = RequestMethod.GET)
  public ModelAndView mostrarStock() {
    ModelMap modelo = new ModelMap();
    modelo.put("stockArticulos", servicioLote.obtenerStockAgrupado());
    return new ModelAndView("stock", modelo);
  }

  @RequestMapping(path = "/admin/lotes/{id}", method = RequestMethod.GET)
  public ModelAndView mostrarDetalleLote(@PathVariable Long id) {
    ModelMap modelo = new ModelMap();
    Lote lote = servicioLote.buscarPorId(id);
    List<Pedido> pedidos = servicioLote.obtenerPedidosQueUsaronLote(id);
    modelo.put("lote", lote);
    modelo.put("pedidos", pedidos);
    return new ModelAndView("funcionalidadesAdmin/lote/detalle-lote", modelo);
  }

  @RequestMapping(path = "/admin/nuevo-articulo", method = RequestMethod.GET)
  public ModelAndView mostrarFormularioNuevoLote() {
    ModelMap modelo = new ModelMap();
    modelo.put("articulo", new Lote());
    modelo.put(PRODUCTOS, servicioProducto.obtenerTodosLosProductos());
    return new ModelAndView(VISTA_NUEVO_LOTE, modelo);
  }

  @RequestMapping(path = "/admin/nuevo-articulo", method = RequestMethod.POST)
  public ModelAndView registrarLote(
    @ModelAttribute("articulo") Lote lote,
    @RequestParam("productoId") Long productoId,
    @RequestParam("fechaIngresoStr") String fechaIngresoStr,
    @RequestParam("fechaVencimientoStr") String fechaVencimientoStr
  ) {
    try {
      asignarProducto(lote, productoId);
      asignarFechas(lote, fechaIngresoStr, fechaVencimientoStr);
      servicioLote.registrarLote(lote);
      return new ModelAndView("redirect:/admin");
    } catch (Exception exception) {
      return formularioConError(lote, exception);
    }
  }

  private void asignarProducto(Lote lote, Long productoId) {
    Producto producto = servicioProducto.obtenerProductoPorId(productoId);
    if (producto == null) {
      throw new IllegalArgumentException("El producto seleccionado no existe");
    }
    lote.setProducto(producto);
  }

  private void asignarFechas(Lote lote, String fechaIngresoStr, String fechaVencimientoStr) {
    if (fechaIngresoStr != null && !fechaIngresoStr.isEmpty()) {
      lote.setFechaDeIngreso(LocalDateTime.parse(fechaIngresoStr).atOffset(ZoneOffset.UTC));
    }
    if (fechaVencimientoStr != null && !fechaVencimientoStr.isEmpty()) {
      lote.setFechaDeVencimiento(LocalDateTime.parse(fechaVencimientoStr).atOffset(ZoneOffset.UTC));
    }
  }

  private ModelAndView formularioConError(Lote lote, Exception exception) {
    ModelMap modelo = new ModelMap();
    modelo.put("articulo", lote);
    modelo.put(PRODUCTOS, servicioProducto.obtenerTodosLosProductos());
    modelo.put("error", "Error al registrar el lote: " + exception.getMessage());
    return new ModelAndView(VISTA_NUEVO_LOTE, modelo);
  }
}
