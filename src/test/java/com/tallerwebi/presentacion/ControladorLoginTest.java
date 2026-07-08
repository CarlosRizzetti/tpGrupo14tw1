package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import com.tallerwebi.dominio.interfaces.ServicioValidacionIdentidad;
import com.tallerwebi.presentacion.controller.ControladorLogin;
import com.tallerwebi.presentacion.dto.LoginDto;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLoginTest {

  private ControladorLogin controladorLogin;
  private Usuario usuarioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioLogin servicioLoginMock;
  private ServicioValidacionIdentidad servicioValidacionIdentidadMock;
  private Authentication authenticationMock;

  @BeforeEach
  public void init() {
    usuarioMock = mock(Usuario.class);
    when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioLoginMock = mock(ServicioLogin.class);
    servicioValidacionIdentidadMock = mock(ServicioValidacionIdentidad.class);
    controladorLogin = new ControladorLogin(servicioLoginMock, servicioValidacionIdentidadMock);
    this.authenticationMock = mock(Authentication.class);
  }

  @Test
  public void registrameSiUsuarioNoExisteDeberiaCrearUsuarioYRedirigirAValidacion()
    throws Exception {
    // preparacion
    when(requestMock.getSession()).thenReturn(sessionMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/validacion-identidad"));
    verify(servicioLoginMock, times(1)).registrar(usuarioMock);
    verify(servicioValidacionIdentidadMock, times(1)).solicitarValidacion(usuarioMock.getEmail());
  }

  @Test
  public void registrarmeSiUsuarioExisteDeberiaVolverAFormularioYMostrarError() throws Exception {
    // preparacion
    doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("loginYRegistro/nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El usuario ya existe")
    );
  }

  @Test
  public void registrarmeSiPasswordEsInvalidaDeberiaVolverAFormularioYMostrarError()
    throws Exception {
    // preparacion
    doThrow(new com.tallerwebi.dominio.excepcion.PasswordInvalida("Password invalida"))
      .when(servicioLoginMock)
      .registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("loginYRegistro/nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Password invalida")
    );
  }

  @Test
  public void errorEnRegistrarmeDeberiaVolverAFormularioYMostrarError() throws Exception {
    // preparacion
    doThrow(RuntimeException.class).when(servicioLoginMock).registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("loginYRegistro/nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Error al registrar el nuevo usuario")
    );
  }

  @Test
  public void irALoginDeberiaRetornarVistaLoginConDatosLogin() {
    when(authenticationMock.isAuthenticated()).thenReturn(false);
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irALogin(authenticationMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void loginConErrorDeberiaMostrarMensajeDeLaExcepcion() {
    // preparacion
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("SPRING_SECURITY_LAST_EXCEPTION"))
      .thenReturn(new RuntimeException("Mi error personalizado"));

    // ejecucion
    ModelAndView modelAndView = controladorLogin.login("true", requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("loginYRegistro/login"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Usuario o clave incorrecta")
    );
  }

  @Test
  public void loginSinErrorDeberiaMostrarFormularioVacio() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.login(null, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("loginYRegistro/login"));
  }

  @Test
  public void nuevoUsuarioDeberiaRetornarVistaNuevoUsuarioConUsuarioVacio() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.nuevoUsuario();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("loginYRegistro/nuevo-usuario"));
    assertThat(modelAndView.getModel().get("usuario"), instanceOf(Usuario.class));
  }

  @Test
  public void validarLoginConUsuarioValidoDeberiaRedirigirAHome() throws Exception {
    LoginDto loginDto = new LoginDto();
    loginDto.setEmail("dami@unlam.com");
    loginDto.setPassword("1234");

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuario("dami@unlam.com", "1234")).thenReturn(usuarioMock);

    when(usuarioMock.getRol()).thenReturn("ADMIN");
    when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");

    ModelAndView mav = controladorLogin.validarLogin(loginDto, requestMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/home"));

    verify(sessionMock).setAttribute("ROL", "ADMIN");
    verify(sessionMock).setAttribute("EMAIL", "dami@unlam.com");
  }

  @Test
  public void validarLoginConUsuarioInexistenteDeberiaMostrarError() throws Exception {
    LoginDto loginDto = new LoginDto();
    loginDto.setEmail("dami@unlam.com");
    loginDto.setPassword("1234");

    when(servicioLoginMock.consultarUsuario("dami@unlam.com", "1234")).thenReturn(null);

    ModelAndView mav = controladorLogin.validarLogin(loginDto, requestMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("loginYRegistro/login"));
    assertThat(
      mav.getModel().get("error").toString(),
      equalToIgnoringCase("Usuario o clave incorrecta")
    );
  }

  @Test
  public void validarLoginConPasswordInvalidaDeberiaLanzarRuntimeException() throws Exception {
    LoginDto loginDto = new LoginDto();
    loginDto.setEmail("dami@unlam.com");
    loginDto.setPassword("1234");

    when(servicioLoginMock.consultarUsuario("dami@unlam.com", "1234"))
      .thenThrow(new PasswordInvalida("Password inválida"));

    assertThrows(
      RuntimeException.class,
      () -> {
        controladorLogin.validarLogin(loginDto, requestMock);
      }
    );
  }
}
