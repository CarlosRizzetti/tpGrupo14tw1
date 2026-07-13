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
  private static final String REDIRECT_ACCESO_DENEGADO = "redirect:/acceso-denegado";

  private final ServicioTelegram servicioTelegram;

  /**
   * Constructor del controlador.
   *
   * @param servicioArticulo servicio para la gestión de artículos
   * @param servicioTelegram servicio para el envío de mensajes a Telegram
   */
  @Autowired
  public ControladorAdminPanel(ServicioLote servicioLote, ServicioTelegram servicioTelegram) {
    this.servicioLote = servicioLote;
    this.servicioTelegram = servicioTelegram;
  }

  @RequestMapping(path = "/admin", method = RequestMethod.GET)
  public ModelAndView panelDeControl(Authentication authentication) {
    if (authentication == null) {
      return new ModelAndView("redirect:/login");
    }
    if (authentication == null || !authentication.isAuthenticated()) {
      return new ModelAndView(REDIRECT_ACCESO_DENEGADO);
    }

    boolean isAdmin = authentication
      .getAuthorities()
      .stream()
      .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    if (!isAdmin) {
      return new ModelAndView(REDIRECT_ACCESO_DENEGADO);
    }

    ModelMap model = new ModelMap();
    model.put("email", authentication.getName());
    model.put("notificaciones", servicioLote.obtenerNotificacionesVencimiento());

    return new ModelAndView("funcionalidadesAdmin/panel", model);
  }

  /**
   * Envía un mensaje y una notificación de prueba a Telegram y redirige al panel.
   *
   * @param authentication detalles de autenticación del usuario actual
   * @return ModelAndView para redireccionar al panel de administración con
   *         parámetro de éxito
   */
  @RequestMapping(path = "/admin/probar-telegram", method = RequestMethod.GET)
  public ModelAndView probarTelegram(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return new ModelAndView(REDIRECT_ACCESO_DENEGADO);
    }

    boolean isAdmin = authentication
      .getAuthorities()
      .stream()
      .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    if (!isAdmin) {
      return new ModelAndView(REDIRECT_ACCESO_DENEGADO);
    }

    servicioTelegram.enviarMensaje(
      "🔔 ¡Prueba de conexión de Telegram exitosa desde el Panel de Administración!"
    );
    servicioTelegram.enviarNotificacionesVencimiento();

    return new ModelAndView("redirect:/admin?telegramOk=true");
  }

  /**
   * Muestra la vista de acceso denegado.
   *
   * @return ModelAndView que renderiza la vista de acceso denegado
   */
  @RequestMapping(path = "/acceso-denegado", method = RequestMethod.GET)
  public ModelAndView accesoDenegado() {
    return new ModelAndView("acceso-denegado");
  }
}
