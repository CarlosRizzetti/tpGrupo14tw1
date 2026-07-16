package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioHistorialPedido;
import com.tallerwebi.presentacion.dto.HistorialPedidoDTO;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorHistorialPedido {

  private static final ZoneOffset OFFSET = ZoneOffset.ofHours(-3);

  private final ServicioHistorialPedido servicioHistorialPedido;

  @Autowired
  public ControladorHistorialPedido(ServicioHistorialPedido servicioHistorialPedido) {
    this.servicioHistorialPedido = servicioHistorialPedido;
  }

  @RequestMapping(path = "/admin/historial-pedidos", method = RequestMethod.GET)
  public ModelAndView mostrarHistorial() {
    return new ModelAndView("funcionalidadesAdmin/pedido/historial-pedidos", new ModelMap());
  }

  @RequestMapping(path = "/admin/historial-pedidos/buscar", method = RequestMethod.GET)
  @ResponseBody
  public List<HistorialPedidoDTO> buscarHistorial(
    @RequestParam(name = "desde", required = false) String desdeStr,
    @RequestParam(name = "hasta", required = false) String hastaStr,
    @RequestParam(name = "numeroDeLote", required = false) Long numeroDeLote,
    @RequestParam(name = "clienteNombre", required = false) String clienteNombre
  ) {
    OffsetDateTime desde = parsearInicioDeDia(desdeStr);
    OffsetDateTime hasta = parsearFinDeDia(hastaStr);
    return servicioHistorialPedido.buscarHistorial(desde, hasta, numeroDeLote, clienteNombre);
  }

  @RequestMapping(path = "/admin/pedidos/{id}", method = RequestMethod.GET)
  public ModelAndView mostrarDetallePedido(@PathVariable Long id) {
    HistorialPedidoDTO pedido = servicioHistorialPedido.buscarPorId(id);
    if (pedido == null) {
      return new ModelAndView("redirect:/admin/historial-pedidos");
    }
    ModelAndView mav = new ModelAndView("funcionalidadesAdmin/pedido/detalle-pedido.html");
    mav.addObject("pedido", pedido);
    return mav;
  }

  private OffsetDateTime parsearInicioDeDia(String fechaStr) {
    if (fechaStr == null || fechaStr.isBlank()) {
      return null;
    }
    return LocalDate.parse(fechaStr).atStartOfDay().atOffset(OFFSET);
  }

  private OffsetDateTime parsearFinDeDia(String fechaStr) {
    if (fechaStr == null || fechaStr.isBlank()) {
      return null;
    }
    return LocalDate.parse(fechaStr).atTime(23, 59, 59).atOffset(OFFSET);
  }
}
