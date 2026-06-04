package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import com.tallerwebi.presentacion.dto.UsuarioDto;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorUsuario {

  private static final String ERROR = "error";
  private static final String VISTA_LISTA = "usuario/lista";
  private static final String VISTA_NUEVO = "usuario/nuevo";
  private static final String VISTA_EDITAR = "usuario/editar";
  private static final String REDIRECT_LISTA = "redirect:/admin/usuarios";
  private static final String REDIRECT_DENEGADO = "redirect:/acceso-denegado";
  private static final String ATTR_DTO = "usuarioDto";

  private final ServicioUsuario servicioUsuario;

  @Autowired
  public ControladorUsuario(ServicioUsuario servicioUsuario) {
    this.servicioUsuario = servicioUsuario;
  }

  @RequestMapping(value = "/admin/usuarios", method = RequestMethod.GET)
  public ModelAndView listarUsuarios(HttpSession session) {
    if (!esAdministrador(session)) {
      return new ModelAndView(REDIRECT_DENEGADO);
    }
    List<Usuario> usuarios = servicioUsuario.listarUsuarios();
    ModelMap modelo = new ModelMap();
    modelo.put("usuarios", usuarios);
    return new ModelAndView(VISTA_LISTA, modelo);
  }

  @RequestMapping(value = "/admin/usuarios/nuevo", method = RequestMethod.GET)
  public ModelAndView mostrarFormularioNuevo(HttpSession session) {
    if (!esAdministrador(session)) {
      return new ModelAndView(REDIRECT_DENEGADO);
    }
    ModelMap modelo = new ModelMap();
    modelo.put(ATTR_DTO, new UsuarioDto());
    return new ModelAndView(VISTA_NUEVO, modelo);
  }

  @RequestMapping(value = "/admin/usuarios/nuevo", method = RequestMethod.POST)
  public ModelAndView crearUsuario(
    @ModelAttribute(ATTR_DTO) UsuarioDto usuarioDto,
    HttpSession session
  ) {
    if (!esAdministrador(session)) {
      return new ModelAndView(REDIRECT_DENEGADO);
    }
    try {
      servicioUsuario.crearUsuario(usuarioDto);
      return new ModelAndView(REDIRECT_LISTA);
    } catch (UsuarioExistente e) {
      ModelMap modelo = new ModelMap();
      modelo.put(ERROR, "El usuario ya existe");
      modelo.put(ATTR_DTO, usuarioDto);
      return new ModelAndView(VISTA_NUEVO, modelo);
    } catch (PasswordInvalida e) {
      ModelMap modelo = new ModelMap();
      modelo.put(ERROR, e.getMessage());
      modelo.put(ATTR_DTO, usuarioDto);
      return new ModelAndView(VISTA_NUEVO, modelo);
    }
  }

  @RequestMapping(value = "/admin/usuarios/{id}/editar", method = RequestMethod.GET)
  public ModelAndView mostrarFormularioEditar(@PathVariable Long id, HttpSession session) {
    if (!esAdministrador(session)) {
      return new ModelAndView(REDIRECT_DENEGADO);
    }
    Usuario usuario = servicioUsuario.obtenerUsuarioPorId(id);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LISTA);
    }
    UsuarioDto dto = new UsuarioDto();
    dto.setEmail(usuario.getEmail());
    dto.setRol(usuario.getRol());
    ModelMap modelo = new ModelMap();
    modelo.put(ATTR_DTO, dto);
    modelo.put("usuarioId", id);
    return new ModelAndView(VISTA_EDITAR, modelo);
  }

  @RequestMapping(value = "/admin/usuarios/{id}/editar", method = RequestMethod.POST)
  public ModelAndView editarUsuario(
    @PathVariable Long id,
    @ModelAttribute(ATTR_DTO) UsuarioDto usuarioDto,
    HttpSession session
  ) {
    if (!esAdministrador(session)) {
      return new ModelAndView(REDIRECT_DENEGADO);
    }
    try {
      servicioUsuario.editarUsuario(id, usuarioDto);
      return new ModelAndView(REDIRECT_LISTA);
    } catch (com.tallerwebi.dominio.excepcion.PasswordInvalida e) {
      ModelMap modelo = new ModelMap();
      modelo.put(ERROR, e.getMessage());
      modelo.put(ATTR_DTO, usuarioDto);
      modelo.put("usuarioId", id);
      return new ModelAndView(VISTA_EDITAR, modelo);
    } catch (IllegalArgumentException e) {
      ModelMap modelo = new ModelMap();
      modelo.put(ERROR, e.getMessage());
      modelo.put(ATTR_DTO, usuarioDto);
      modelo.put("usuarioId", id);
      return new ModelAndView(VISTA_EDITAR, modelo);
    }
  }

  @RequestMapping(value = "/admin/usuarios/{id}/reactivar", method = RequestMethod.POST)
  public ModelAndView reactivar(@PathVariable Long id, HttpSession session) {
    if (!esAdministrador(session)) {
      return new ModelAndView(REDIRECT_DENEGADO);
    }
    servicioUsuario.reactivar(id);
    return new ModelAndView(REDIRECT_LISTA);
  }

  @RequestMapping(value = "/admin/usuarios/{id}/dar-de-baja", method = RequestMethod.POST)
  public ModelAndView darDeBaja(@PathVariable Long id, HttpSession session) {
    if (!esAdministrador(session)) {
      return new ModelAndView(REDIRECT_DENEGADO);
    }
    servicioUsuario.darDeBaja(id);
    return new ModelAndView(REDIRECT_LISTA);
  }

  private boolean esAdministrador(HttpSession session) {
    Object rol = session.getAttribute("ROL");
    if (rol == null) return false;
    return "ADMIN".equalsIgnoreCase(rol.toString());
  }
}
