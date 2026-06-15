package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import com.tallerwebi.dominio.interfaces.ServicioValidacionIdentidad;
import com.tallerwebi.presentacion.dto.LoginDto;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogin {

  private static final String ERROR = "error";
  private static final String VISTA_NUEVO_USUARIO = "loginYRegistro/nuevo-usuario";
  private static final String REDIRECT_VALIDACION = "redirect:/validacion-identidad";
  private final ServicioLogin servicioLogin;
  private final ServicioValidacionIdentidad servicioValidacionIdentidad;

  @Autowired
  public ControladorLogin(
    ServicioLogin servicioLogin,
    ServicioValidacionIdentidad servicioValidacionIdentidad
  ) {
    this.servicioLogin = servicioLogin;
    this.servicioValidacionIdentidad = servicioValidacionIdentidad;
  }

  @RequestMapping("/")
  public ModelAndView irALogin(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      return new ModelAndView("redirect:/home");
    }
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping("/login")
  public ModelAndView login() {
    ModelMap modelo = new ModelMap();
    modelo.put("loginDto", new LoginDto());
    return new ModelAndView("loginYRegistro/login", modelo);
  }

  @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
  public ModelAndView registrarme(
    @ModelAttribute("usuario") Usuario usuario,
    HttpServletRequest request
  ) {
    ModelMap model = new ModelMap();
    try {
      servicioLogin.registrar(usuario);
      servicioValidacionIdentidad.solicitarValidacion(usuario.getEmail());
    } catch (UsuarioExistente e) {
      model.put(ERROR, "El usuario ya existe");
      return new ModelAndView(VISTA_NUEVO_USUARIO, model);
    } catch (com.tallerwebi.dominio.excepcion.PasswordInvalida e) {
      model.put(ERROR, e.getMessage());
      return new ModelAndView(VISTA_NUEVO_USUARIO, model);
    } catch (Exception e) {
      model.put(ERROR, "Error al registrar el nuevo usuario");
      return new ModelAndView(VISTA_NUEVO_USUARIO, model);
    }
    return new ModelAndView(REDIRECT_VALIDACION);
  }

  @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
  public ModelAndView nuevoUsuario() {
    ModelMap model = new ModelMap();
    model.put("usuario", new Usuario());
    return new ModelAndView(VISTA_NUEVO_USUARIO, model);
  }
}
