package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInactivo;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogin {

  private static final String ERROR = "error";
  private static final String VISTA_LOGIN = "loginYRegistro/login";
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

  // Login normal — no se toca, tests existentes siguen pasando
  @RequestMapping("/login")
  public ModelAndView login(
    @RequestParam(value = "error", required = false) String errorParam,
    HttpServletRequest request
  ) {
    ModelMap modelo = new ModelMap();
    if (errorParam != null) {
      Exception exception = (Exception) request
        .getSession()
        .getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
      if (exception != null) {
        modelo.put(ERROR, exception.getMessage());
      } else {
        modelo.put(ERROR, "Usuario o clave incorrecta");
      }
    }
    modelo.put("loginDto", new LoginDto());
    return new ModelAndView(VISTA_LOGIN, modelo);
  }

  // Login OAuth2 — maneja errores de Google
  @RequestMapping("/login-oauth")
  public ModelAndView loginOauth(
    @RequestParam(value = "pendiente", required = false) String pendiente,
    @RequestParam(value = "sinCategorias", required = false) String sinCategorias
  ) {
    ModelMap modelo = new ModelMap();
    if (pendiente != null) {
      modelo.put(ERROR, "Tu cuenta está pendiente de activación");
    }
    if (sinCategorias != null) {
      modelo.put(ERROR, "Todavía no te asignaron una categoría");
    }
    modelo.put("loginDto", new LoginDto());
    return new ModelAndView(VISTA_LOGIN, modelo);
  }

  // Código muerto
  @Deprecated
  @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
  public ModelAndView validarLogin(
    @ModelAttribute("loginDto") LoginDto loginDto,
    HttpServletRequest request
  ) {
    try {
      Usuario usuarioBuscado = servicioLogin.consultarUsuario(
        loginDto.getEmail(),
        loginDto.getPassword()
      );
      if (usuarioBuscado != null) {
        request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
        request.getSession().setAttribute("EMAIL", usuarioBuscado.getEmail());
        return new ModelAndView("redirect:/home");
      } else {
        ModelMap model = new ModelMap();
        model.put(ERROR, "Usuario o clave incorrecta");
        return new ModelAndView(VISTA_LOGIN, model);
      }
    } catch (UsuarioInactivo e) {
      ModelMap model = new ModelMap();
      model.put(ERROR, "El usuario está inactivo");
      return new ModelAndView(VISTA_LOGIN, model);
    } catch (PasswordInvalida e) {
      throw new RuntimeException(e);
    }
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
