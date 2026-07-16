package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.evento.InteraccionChatEvent;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import com.tallerwebi.dominio.interfaces.ServicioPedido;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorPortalCliente {

  private static final String ATTR_ERROR = "error";
  private static final String ATTR_CLIENTE = "cliente";
  private static final String REDIRECT_PORTAL_CLIENTES = "redirect:/portal/clientes";
  private static final String REDIRECT_COMPLETAR_DATOS =
    "redirect:/portal/clientes/completar-datos";
  private static final String REDIRECT_HOME = "redirect:/portal/clientes/home";

  private final ServicioCliente servicioCliente;
  private final ServicioPedido servicioPedido;

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public ControladorPortalCliente(
    ServicioCliente servicioCliente,
    ServicioPedido servicioPedido,
    ApplicationEventPublisher eventPublisher
  ) {
    this.servicioCliente = servicioCliente;
    this.eventPublisher = eventPublisher;
    this.servicioPedido = servicioPedido;
  }

  @ModelAttribute("categoria")
  public CategoriaDto categoriaTema() {
    return new CategoriaDto(new Categoria("servicio", true, "servicio"));
  }

  @GetMapping("/portal/clientes")
  public String mostrarLoginCliente(
    @RequestParam(value = "error", required = false) String error,
    Model model
  ) {
    if (error != null) {
      model.addAttribute(ATTR_ERROR, "Correo/DNI o contraseña incorrectos");
    }
    return "portalCliente/login";
  }

  @PostMapping("/portal/clientes/procesar")
  public String procesarLoginFallback() {
    return "redirect:/portal/clientes?error=true";
  }

  @GetMapping("/portal/clientes/registro")
  public String mostrarRegistroCliente(Model model) {
    model.addAttribute(ATTR_CLIENTE, new Cliente());
    return "portalCliente/registro";
  }

  @PostMapping("/portal/clientes/registro-procesar")
  public String procesarRegistroCliente(
    @ModelAttribute("cliente") Cliente cliente,
    Model model,
    HttpServletRequest request
  ) {
    try {
      String rawPassword = cliente.getPassword();
      servicioCliente.registrarCliente(cliente);

      // Registro exitoso, activación inmediata. Logueamos al usuario automáticamente:
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        cliente,
        rawPassword,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
      );
      SecurityContextHolder.getContext().setAuthentication(auth);
      request
        .getSession()
        .setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

      return REDIRECT_HOME;
    } catch (Exception e) {
      model.addAttribute(ATTR_ERROR, e.getMessage());
      model.addAttribute(ATTR_CLIENTE, cliente);
      return "portalCliente/registro";
    }
  }

  @GetMapping("/portal/clientes/google-login")
  public String redirigirGoogleLoginCliente(HttpServletRequest request) {
    request.getSession().setAttribute("OAUTH_LOGIN_TYPE", "CLIENTE");
    return "redirect:/oauth2/authorization/google";
  }

  @GetMapping("/portal/clientes/completar-datos")
  public String mostrarCompletarDatosCliente(Authentication authentication, Model model) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente == null) {
      return REDIRECT_PORTAL_CLIENTES;
    }
    if (!faltanDatosObligatorios(cliente)) {
      return REDIRECT_HOME;
    }
    model.addAttribute(ATTR_CLIENTE, cliente);
    return "portalCliente/completar-datos";
  }

  @PostMapping("/portal/clientes/completar-datos-procesar")
  public String procesarCompletarDatosCliente(
    @RequestParam(value = "documento", required = false) String documento,
    @RequestParam(value = "telefono", required = false) String telefono,
    Authentication authentication,
    Model model,
    HttpServletRequest request
  ) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente == null) {
      return REDIRECT_PORTAL_CLIENTES;
    }
    try {
      if (
        documento == null ||
        documento.trim().isEmpty() ||
        telefono == null ||
        telefono.trim().isEmpty()
      ) {
        throw new Exception("Por favor, ingresa tanto tu número de DNI como tu teléfono celular.");
      }
      servicioCliente.actualizarDatosCliente(cliente, documento, telefono, cliente.getNombre());

      // Actualizar sesión de seguridad en Spring Security
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        cliente,
        authentication.getCredentials(),
        authentication.getAuthorities()
      );
      SecurityContextHolder.getContext().setAuthentication(auth);
      request
        .getSession()
        .setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

      return REDIRECT_HOME;
    } catch (Exception e) {
      model.addAttribute(ATTR_ERROR, e.getMessage());
      model.addAttribute(ATTR_CLIENTE, cliente);
      return "portalCliente/completar-datos";
    }
  }

  @GetMapping("/portal/clientes/home")
  public String mostrarHome(Authentication authentication, Model model) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente != null) {
      if (faltanDatosObligatorios(cliente)) {
        return REDIRECT_COMPLETAR_DATOS;
      }
      model.addAttribute("faltanDatos", false);
      model.addAttribute(ATTR_CLIENTE, cliente);
    }
    return "portalCliente/home";
  }

  @GetMapping("/portal/clientes/mis-pedidos")
  public String redirigirMisPedidosAHome() {
    return REDIRECT_HOME;
  }

  @GetMapping("/portal/clientes/perfil")
  public String mostrarPerfilCliente(Authentication authentication, Model model) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente == null) {
      return REDIRECT_PORTAL_CLIENTES;
    }
    model.addAttribute(ATTR_CLIENTE, cliente);
    return "portalCliente/perfil";
  }

  @PostMapping("/portal/clientes/perfil-guardar")
  public String guardarPerfilCliente(
    @RequestParam(value = "documento", required = false) String documento,
    @RequestParam(value = "telefono", required = false) String telefono,
    @RequestParam(value = "nombre", required = false) String nombre,
    Authentication authentication,
    Model model,
    HttpServletRequest request
  ) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente == null) {
      return REDIRECT_PORTAL_CLIENTES;
    }
    try {
      servicioCliente.actualizarDatosCliente(cliente, documento, telefono, nombre);
      // Actualizar sesión de seguridad
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        cliente,
        authentication.getCredentials(),
        authentication.getAuthorities()
      );
      SecurityContextHolder.getContext().setAuthentication(auth);
      request
        .getSession()
        .setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

      model.addAttribute("exito", "¡Tus datos han sido actualizados correctamente!");
      model.addAttribute(ATTR_CLIENTE, cliente);
      return "portalCliente/perfil";
    } catch (Exception e) {
      model.addAttribute(ATTR_ERROR, e.getMessage());
      model.addAttribute(ATTR_CLIENTE, cliente);
      return "portalCliente/perfil";
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private Cliente obtenerClienteSesion(Authentication authentication) {
    if (authentication == null) return null;
    Object principal = authentication.getPrincipal();
    if (principal instanceof Cliente) {
      Cliente enSesion = (Cliente) principal;
      Cliente bd = null;
      if (enSesion.getEmail() != null && !enSesion.getEmail().trim().isEmpty()) {
        bd = servicioCliente.buscarPorEmail(enSesion.getEmail());
      }
      if (
        bd == null && enSesion.getDocumento() != null && !enSesion.getDocumento().trim().isEmpty()
      ) {
        bd = servicioCliente.buscarPorDocumento(enSesion.getDocumento());
      }
      return bd != null ? bd : enSesion;
    }
    return null;
  }

  private boolean faltanDatosObligatorios(Cliente cliente) {
    if (cliente == null) return false;
    return (
      cliente.getDocumento() == null ||
      cliente.getDocumento().trim().isEmpty() ||
      cliente.getTelefono() == null ||
      cliente.getTelefono().trim().isEmpty()
    );
  }

  @GetMapping("/portal/clientes/historial")
  public String mostrarHistorialPedidos(Authentication authentication, Model model) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente == null) {
      return REDIRECT_PORTAL_CLIENTES;
    }
    if (faltanDatosObligatorios(cliente)) {
      return REDIRECT_COMPLETAR_DATOS;
    }
    List<Pedido> pedidos = servicioPedido.listarPedidosDeCliente(cliente);
    model.addAttribute("pedidos", pedidos);
    model.addAttribute(ATTR_CLIENTE, cliente);
    return "portalCliente/historial";
  }

  @GetMapping("/portal/clientes/reportar")
  public String mostrarReportarPedido(
    @RequestParam(value = "idPedido", required = false) Long idPedido,
    Authentication authentication,
    Model model
  ) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente == null) {
      return REDIRECT_PORTAL_CLIENTES;
    }
    if (faltanDatosObligatorios(cliente)) {
      return REDIRECT_COMPLETAR_DATOS;
    }
    if (idPedido != null) {
      Pedido pedido = servicioPedido.buscarPedidoPorId(idPedido);
      if (pedido != null) {
        model.addAttribute("pedido", pedido);
      }
      model.addAttribute("idPedido", idPedido);
    }
    model.addAttribute(ATTR_CLIENTE, cliente);
    return "portalCliente/reportar";
  }

  @PostMapping("/reportar/accion")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> procesarAccionChat(
    @RequestParam("pedidoId") Long pedidoId,
    @RequestParam("accion") String accion
  ) {
    InteraccionChatEvent evento = new InteraccionChatEvent(pedidoId, accion);

    eventPublisher.publishEvent(evento);

    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put("mensaje", evento.getRespuestaSistema());
    respuesta.put("opciones", evento.getOpcionesDisponibles());

    return ResponseEntity.ok(respuesta);
  }

  public String mostrarReportarPedido(Authentication authentication, Model model) {
    return mostrarReportarPedido(null, authentication, model);
  }

  @PostMapping("/portal/clientes/reportar")
  public String procesarReportarPedido(
    @RequestParam(value = "idPedido", required = false) Long idPedido,
    @RequestParam(value = "motivo", required = false) String motivo,
    @RequestParam(value = "comentario", required = false) String comentario,
    Authentication authentication,
    RedirectAttributes redirectAttributes
  ) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente == null) {
      return REDIRECT_PORTAL_CLIENTES;
    }
    if (faltanDatosObligatorios(cliente)) {
      return REDIRECT_COMPLETAR_DATOS;
    }
    if (idPedido != null) {
      servicioPedido.marcarPedidoComoReportado(idPedido, motivo, comentario);
    }
    String numPedido = idPedido != null ? ("#" + idPedido) : "seleccionado";
    redirectAttributes.addFlashAttribute(
      "mensajeExito",
      "Tu reporte del pedido " +
      numPedido +
      " ha sido enviado. Nuestro equipo lo revisará a la brevedad."
    );
    return "redirect:/portal/clientes/historial";
  }
}
