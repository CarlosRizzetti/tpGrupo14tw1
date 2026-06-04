package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import com.tallerwebi.presentacion.controller.ControladorUsuario;
import com.tallerwebi.presentacion.dto.UsuarioDto;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorUsuarioTest {

  private ControladorUsuario controladorUsuario;
  private ServicioUsuario servicioUsuarioMock;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    servicioUsuarioMock = mock(ServicioUsuario.class);
    sessionMock = mock(HttpSession.class);
    controladorUsuario = new ControladorUsuario(servicioUsuarioMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("ADMIN");
  }

  @Test
  public void listarUsuariosDeberiaRetornarVistaListaConUsuarios() {
    // preparacion
    List<Usuario> usuarios = Arrays.asList(new Usuario(), new Usuario());
    when(servicioUsuarioMock.listarUsuarios()).thenReturn(usuarios);

    // ejecucion
    ModelAndView mav = controladorUsuario.listarUsuarios(sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("usuario/lista"));
    assertThat(mav.getModel().get("usuarios"), equalTo(usuarios));
  }

  @Test
  public void listarUsuariosSinRolAdminDeberiaRedirigirAAccesoDenegado() {
    // preparacion
    when(sessionMock.getAttribute("ROL")).thenReturn("USER");

    // ejecucion
    ModelAndView mav = controladorUsuario.listarUsuarios(sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/acceso-denegado"));
  }

  @Test
  public void mostrarFormularioNuevoDeberiaRetornarVistaNuevoConDtoVacio() {
    // ejecucion
    ModelAndView mav = controladorUsuario.mostrarFormularioNuevo(sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("usuario/nuevo"));
    assertThat(mav.getModel().get("usuarioDto"), instanceOf(UsuarioDto.class));
  }

  @Test
  public void crearUsuarioConDatosValidosDeberiaRedirigirALista() throws Exception {
    // preparacion
    UsuarioDto dto = new UsuarioDto();

    // ejecucion
    ModelAndView mav = controladorUsuario.crearUsuario(dto, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/usuarios"));
    verify(servicioUsuarioMock, times(1)).crearUsuario(dto);
  }

  @Test
  public void crearUsuarioExistenteDeberiaVolverAlFormularioConError() throws Exception {
    // preparacion
    UsuarioDto dto = new UsuarioDto();
    doThrow(UsuarioExistente.class).when(servicioUsuarioMock).crearUsuario(dto);

    // ejecucion
    ModelAndView mav = controladorUsuario.crearUsuario(dto, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("usuario/nuevo"));
    assertThat(mav.getModel().get("error").toString(), equalToIgnoringCase("El usuario ya existe"));
  }

  @Test
  public void crearUsuarioConPasswordInvalidaDeberiaVolverAlFormularioConError() throws Exception {
    // preparacion
    UsuarioDto dto = new UsuarioDto();
    doThrow(new PasswordInvalida("La contraseña no cumple con los requisitos de seguridad."))
      .when(servicioUsuarioMock)
      .crearUsuario(dto);

    // ejecucion
    ModelAndView mav = controladorUsuario.crearUsuario(dto, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("usuario/nuevo"));
    assertThat(mav.getModel().get("error").toString(), containsString("contraseña"));
  }

  @Test
  public void mostrarFormularioEditarConUsuarioExistenteDeberiaRetornarVistaEditar() {
    // preparacion
    Usuario usuario = new Usuario();
    usuario.setEmail("test@test.com");
    usuario.setRol("USER");
    when(servicioUsuarioMock.obtenerUsuarioPorId(1L)).thenReturn(usuario);

    // ejecucion
    ModelAndView mav = controladorUsuario.mostrarFormularioEditar(1L, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("usuario/editar"));
    assertThat(mav.getModel().get("usuarioId"), equalTo(1L));
  }

  @Test
  public void mostrarFormularioEditarConUsuarioInexistenteDeberiaRedirigirALista() {
    // preparacion
    when(servicioUsuarioMock.obtenerUsuarioPorId(99L)).thenReturn(null);

    // ejecucion
    ModelAndView mav = controladorUsuario.mostrarFormularioEditar(99L, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/usuarios"));
  }

  @Test
  public void editarUsuarioConDatosValidosDeberiaRedirigirALista() throws Exception {
    // preparacion
    UsuarioDto dto = new UsuarioDto();

    // ejecucion
    ModelAndView mav = controladorUsuario.editarUsuario(1L, dto, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/usuarios"));
    verify(servicioUsuarioMock, times(1)).editarUsuario(1L, dto);
  }

  @Test
  public void darDeBajaDeberiaLlamarAlServicioYRedirigirALista() {
    // ejecucion
    ModelAndView mav = controladorUsuario.darDeBaja(1L, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/usuarios"));
    verify(servicioUsuarioMock, times(1)).darDeBaja(1L);
  }

  @Test
  public void reactivarDeberiaLlamarAlServicioYRedirigirALista() {
    // ejecucion
    ModelAndView mav = controladorUsuario.reactivar(1L, sessionMock);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/usuarios"));
    verify(servicioUsuarioMock, times(1)).reactivar(1L);
  }
}
