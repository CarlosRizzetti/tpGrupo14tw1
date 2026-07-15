package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import com.tallerwebi.dominio.interfaces.ServicioHistorialPedido;
import com.tallerwebi.presentacion.dto.HistorialPedidoDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/clientes")
public class ControladorCliente {

  private final ServicioCliente servicioCliente;
  private final ServicioHistorialPedido servicioHistorialPedido;

  @Autowired
  public ControladorCliente(
    ServicioCliente servicioCliente,
    ServicioHistorialPedido servicioHistorialPedido
  ) {
    this.servicioCliente = servicioCliente;
    this.servicioHistorialPedido = servicioHistorialPedido;
  }

  @RequestMapping(path = "/{id}", method = RequestMethod.GET)
  public ModelAndView mostrarDetalleCliente(@PathVariable Long id) {
    Cliente cliente = servicioCliente.buscarPorId(id);
    if (cliente == null) {
      return new ModelAndView("redirect:/admin/historial-pedidos");
    }
    List<HistorialPedidoDTO> pedidos = servicioHistorialPedido.buscarPorCliente(id);

    ModelAndView modelAndView = new ModelAndView("funcionalidadesAdmin/cliente/detalle-cliente");
    modelAndView.addObject("cliente", cliente);
    modelAndView.addObject("pedidos", pedidos);
    return modelAndView;
  }
}
