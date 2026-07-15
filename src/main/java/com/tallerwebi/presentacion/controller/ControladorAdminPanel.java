package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.interfaces.ServicioTelegram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdminPanel {

  private final ServicioLote servicioLote;

  private final ServicioTelegram servicioTelegram;

  @Autowired
  public ControladorAdminPanel(ServicioLote servicioLote, ServicioTelegram servicioTelegram) {
    this.servicioLote = servicioLote;
    this.servicioTelegram = servicioTelegram;
  }

  @RequestMapping(path = "/admin", method = RequestMethod.GET)
  public ModelAndView panelDeControl(Authentication authentication) {
    ModelMap model = new ModelMap();
    model.put("email", authentication.getName());
    model.put("notificaciones", servicioLote.obtenerNotificacionesVencimiento());

    return new ModelAndView("funcionalidadesAdmin/panel", model);
  }

  @RequestMapping(path = "/admin/probar-telegram", method = RequestMethod.GET)
  public ModelAndView probarTelegram() {
    servicioTelegram.enviarMensaje(
      "🔔 ¡Prueba de conexión de Telegram exitosa desde el Panel de Administración!"
    );
    servicioTelegram.enviarNotificacionesVencimiento();

    return new ModelAndView("redirect:/admin?telegramOk=true");
  }

  @RequestMapping(path = "/acceso-denegado", method = RequestMethod.GET)
  public ModelAndView accesoDenegado() {
    return new ModelAndView("acceso-denegado");
  }
}
