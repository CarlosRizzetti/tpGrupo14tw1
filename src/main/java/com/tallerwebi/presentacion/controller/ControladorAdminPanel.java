package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.interfaces.ServicioPedido;
import com.tallerwebi.dominio.interfaces.ServicioTelegram;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAdminPanel {

  private final ServicioLote servicioLote;
  private final ServicioPedido servicioPedido;
  private static final String REDIRECT_ACCESO_DENEGADO = "redirect:/acceso-denegado";

  private final ServicioTelegram servicioTelegram;

  @Autowired
  public ControladorAdminPanel(
    ServicioLote servicioLote,
    ServicioTelegram servicioTelegram,
    ServicioPedido servicioPedido
  ) {
    this.servicioLote = servicioLote;
    this.servicioTelegram = servicioTelegram;
    this.servicioPedido = servicioPedido;
  }

  @RequestMapping(path = "/admin", method = RequestMethod.GET)
  public ModelAndView panelDeControl(Authentication authentication) {
    if (authentication == null) {
      return new ModelAndView("redirect:/login");
    }

    ModelMap model = new ModelMap();
    model.put("email", authentication.getName());
    model.put("notificaciones", servicioLote.obtenerNotificacionesVencimiento());
    model.put("reclamosActivos", servicioPedido.contarPedidosReportadosActivos());

    return new ModelAndView("funcionalidadesAdmin/panel", model);
  }

  @RequestMapping(path = "/admin/gestionar-reclamos", method = RequestMethod.GET)
  public ModelAndView gestionarReclamos(Authentication authentication) {
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
    List<Pedido> reclamos = servicioPedido.listarPedidosReportados();
    model.put("reclamos", reclamos);
    model.put("reclamosActivos", servicioPedido.contarPedidosReportadosActivos());
    return new ModelAndView("funcionalidadesAdmin/gestionar-reclamos", model);
  }

  @RequestMapping(path = "/admin/gestionar-reclamos/resolver", method = RequestMethod.POST)
  public ModelAndView resolverReclamo(
    @RequestParam("idPedido") Long idPedido,
    Authentication authentication
  ) {
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

    if (idPedido != null) {
      servicioPedido.resolverReclamoPedido(idPedido);
    }
    return new ModelAndView("redirect:/admin/gestionar-reclamos?resuelto=true");
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
