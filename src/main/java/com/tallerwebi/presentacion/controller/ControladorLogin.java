package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
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

  private ServicioLogin servicioLogin;

  @Autowired
  public ControladorLogin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
  }

  @RequestMapping("/login")
  public ModelAndView irALogin() {
    ModelMap modelo = new ModelMap();
    modelo.put("datosLogin", new LoginDto());
    return new ModelAndView("login", modelo);
  }

  @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
  public ModelAndView validarLogin(
    @ModelAttribute("datosLogin") LoginDto loginDto,
    HttpServletRequest request
  ) {
    Usuario usuarioBuscado = servicioLogin.consultarUsuario(
      loginDto.getEmail(),
      loginDto.getPassword()
    );
    if (usuarioBuscado != null) {
      request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
      return new ModelAndView("redirect:/home");
    } else {
      /* Se instancia el ModelMap solo cuando es necesario (en el flujo de error) para evitar anomalías en el flujo de datos (DU-anomaly de PMD) */
      ModelMap model = new ModelMap();
      model.put("error", "Usuario o clave incorrecta");
      return new ModelAndView("login", model);
    }
  }

  @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
  public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
    ModelMap model = new ModelMap();
    try {
      servicioLogin.registrar(usuario);
    } catch (UsuarioExistente e) {
      model.put("error", "El usuario ya existe");
      return new ModelAndView("nuevo-usuario", model);
    } catch (Exception e) {
      model.put("error", "Error al registrar el nuevo usuario");
      return new ModelAndView("nuevo-usuario", model);
    }
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
  public ModelAndView nuevoUsuario() {
    ModelMap model = new ModelMap();
    model.put("usuario", new Usuario());
    return new ModelAndView("nuevo-usuario", model);
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome() {
    return new ModelAndView("home");
  }
}
