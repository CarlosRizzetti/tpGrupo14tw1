package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorPortalCliente {

  private static final String ATTR_ERROR = "error";
  private static final String ATTR_CLIENTE = "cliente";

  private final ServicioCliente servicioCliente;

  @Autowired
  public ControladorPortalCliente(ServicioCliente servicioCliente) {
    this.servicioCliente = servicioCliente;
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

      return "redirect:/portal/clientes/mis-pedidos";
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

  @GetMapping("/portal/clientes/mis-pedidos")
  public String mostrarMisPedidos(Authentication authentication, Model model) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente != null) {
      boolean faltanDatos =
        (cliente.getDocumento() == null || cliente.getDocumento().trim().isEmpty()) ||
        (cliente.getTelefono() == null || cliente.getTelefono().trim().isEmpty());
      model.addAttribute("faltanDatos", faltanDatos);
      model.addAttribute(ATTR_CLIENTE, cliente);
    }
    return "portalCliente/home";
  }

  @GetMapping("/portal/clientes/perfil")
  public String mostrarPerfilCliente(Authentication authentication, Model model) {
    Cliente cliente = obtenerClienteSesion(authentication);
    if (cliente == null) {
      return "redirect:/portal/clientes";
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
      return "redirect:/portal/clientes";
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

  @GetMapping("/portal/clientes/historial")
  public String mostrarHistorialPedidos() {
    return "portalCliente/historial";
  }

  @GetMapping("/portal/clientes/reportar")
  public String mostrarReportarPedido() {
    return "portalCliente/reportar";
  }
}
