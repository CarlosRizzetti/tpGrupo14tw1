package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.utils.CarritoPedido;
import com.tallerwebi.presentacion.dto.CarritoDTO;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/cajero")
public class ControladorPedidoCajero {

  private static final String ATRIBUTO_SESION_CARRITO = "carrito";
  private final ServicioCategoria servicioCategoria;

  @Autowired
  public ControladorPedidoCajero(ServicioCategoria servicioCategoria) {
    this.servicioCategoria = servicioCategoria;
  }

  @RequestMapping(path = "", method = RequestMethod.GET)
  public ModelAndView mostrarCaja() {
    List<CategoriaDto> categorias = servicioCategoria.obtenerLasCategoriasParaElMenu();

    ModelAndView modelAndView = new ModelAndView("caja/caja");
    modelAndView.addObject("categorias", categorias);
    return modelAndView;
  }

  @RequestMapping(path = "/cobro", method = RequestMethod.GET)
  public ModelAndView mostrarCobro(HttpSession session) {
    CarritoPedido carrito = (CarritoPedido) session.getAttribute(ATRIBUTO_SESION_CARRITO);

    if (carrito == null || carrito.estaVacio()) {
      return new ModelAndView("redirect:/cajero");
    }

    ModelAndView mav = new ModelAndView("caja/cobro");
    mav.addObject("carrito", new CarritoDTO(carrito));
    return mav;
  }
}
