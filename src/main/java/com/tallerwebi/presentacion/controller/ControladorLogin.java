package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInactivo;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import com.tallerwebi.presentacion.dto.LoginDto;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogin {

  private static final String ERROR = "error";
  private static final String VISTA_NUEVO_USUARIO = "nuevo-usuario";
  private ServicioLogin servicioLogin;

  @Autowired
  public ControladorLogin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
  }

  @RequestMapping("/")
  public ModelAndView irALogin() {
    ModelMap modelo = new ModelMap();
    modelo.put("loginDto", new LoginDto());
    return new ModelAndView("login", modelo);
  }

  @RequestMapping("/login")
  public ModelAndView login() {
    return new ModelAndView("redirect:/");
  }

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
        return new ModelAndView("redirect:/home");
      } else {
        ModelMap model = new ModelMap();
        model.put(ERROR, "Usuario o clave incorrecta");
        return new ModelAndView("login", model);
      }
    } catch (UsuarioInactivo e) {
      ModelMap model = new ModelMap();
      model.put(ERROR, "El usuario está inactivo");
      return new ModelAndView("login", model);
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
      request.getSession().setAttribute("ROL", usuario.getRol());
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
    return new ModelAndView("redirect:/home");
  }

  @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
  public ModelAndView nuevoUsuario() {
    ModelMap model = new ModelMap();
    model.put("usuario", new Usuario());
    return new ModelAndView(VISTA_NUEVO_USUARIO, model);
  }
}
