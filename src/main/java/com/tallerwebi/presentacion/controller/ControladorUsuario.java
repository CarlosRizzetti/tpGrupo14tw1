package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import com.tallerwebi.presentacion.dto.UsuarioDto;
import com.tallerwebi.presentacion.dto.UsuarioListadoDto;
import java.util.List;
import java.util.stream.Collectors;
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
  private static final String VISTA_LISTA = "funcionalidadesAdmin/usuario/lista";
  private static final String VISTA_NUEVO = "funcionalidadesAdmin/usuario/nuevo";
  private static final String VISTA_EDITAR = "funcionalidadesAdmin/usuario/editar";
  private static final String REDIRECT_LISTA = "redirect:/admin/usuarios";
  private static final String ATTR_DTO = "usuarioDto";

  private final ServicioUsuario servicioUsuario;
  private final ServicioCategoria servicioCategoria;

  @Autowired
  public ControladorUsuario(ServicioUsuario servicioUsuario, ServicioCategoria servicioCategoria) {
    this.servicioUsuario = servicioUsuario;
    this.servicioCategoria = servicioCategoria;
  }

  @RequestMapping(value = "/admin/usuarios", method = RequestMethod.GET)
  public ModelAndView listarUsuarios() {
    List<UsuarioListadoDto> usuarios = servicioUsuario.listarUsuarios();

    ModelMap modelo = new ModelMap();
    modelo.put("usuarios", usuarios);
    modelo.put(
      "usuariosActivos",
      usuarios
        .stream()
        .filter(usuario -> "ACTIVO".equals(usuario.getEstado()))
        .collect(Collectors.toList())
    );
    modelo.put(
      "usuariosPendientes",
      usuarios
        .stream()
        .filter(usuario -> "PENDIENTE".equals(usuario.getEstado()))
        .collect(Collectors.toList())
    );
    modelo.put(
      "usuariosBaja",
      usuarios
        .stream()
        .filter(usuario -> "BAJA".equals(usuario.getEstado()))
        .collect(Collectors.toList())
    );
    modelo.put("categorias", servicioCategoria.obtenerLasCategoriasParaElMenu());

    return new ModelAndView(VISTA_LISTA, modelo);
  }

  @RequestMapping(value = "/admin/usuarios/nuevo", method = RequestMethod.GET)
  public ModelAndView mostrarFormularioNuevo() {
    ModelMap modelo = new ModelMap();
    modelo.put(ATTR_DTO, new UsuarioDto());
    return new ModelAndView(VISTA_NUEVO, modelo);
  }

  @RequestMapping(value = "/admin/usuarios/nuevo", method = RequestMethod.POST)
  public ModelAndView crearUsuario(@ModelAttribute(ATTR_DTO) UsuarioDto usuarioDto) {
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
  public ModelAndView mostrarFormularioEditar(@PathVariable Long id) {
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
    @ModelAttribute(ATTR_DTO) UsuarioDto usuarioDto
  ) {
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
  public ModelAndView reactivar(@PathVariable Long id) {
    servicioUsuario.reactivar(id);
    return new ModelAndView(REDIRECT_LISTA);
  }

  @RequestMapping(value = "/admin/usuarios/{id}/dar-de-baja", method = RequestMethod.POST)
  public ModelAndView darDeBaja(@PathVariable Long id) {
    servicioUsuario.darDeBaja(id);
    return new ModelAndView(REDIRECT_LISTA);
  }
}
