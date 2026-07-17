package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import com.tallerwebi.dominio.interfaces.ServicioPedido;
import com.tallerwebi.presentacion.controller.ControladorPortalCliente;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

public class ControladorPortalClienteTest {

  private ServicioCliente servicioCliente;
  private ServicioPedido servicioPedido;
  private ControladorPortalCliente controlador;
  private Model model;
  private ApplicationEventPublisher eventPublisher;

  @BeforeEach
  public void init() {
    servicioCliente = mock(ServicioCliente.class);
    servicioPedido = mock(ServicioPedido.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    controlador = new ControladorPortalCliente(servicioCliente, servicioPedido, eventPublisher);
    model = new ConcurrentModel();
  }

  @Test
  @DisplayName("mostrarLoginCliente | Devuelve la vista de login con o sin error")
  public void mostrarLoginCliente() {
    String vistaSinError = controlador.mostrarLoginCliente(null, model);
    assertEquals("portalCliente/login", vistaSinError);

    String vistaConError = controlador.mostrarLoginCliente("true", model);
    assertEquals("portalCliente/login", vistaConError);
    assertEquals("Correo/DNI o contraseña incorrectos", model.getAttribute("error"));
  }

  @Test
  @DisplayName("procesarLoginFallback | Redirigir a login con error")
  public void procesarLoginFallback() {
    assertEquals("redirect:/portal/clientes?error=true", controlador.procesarLoginFallback());
  }

  @Test
  @DisplayName("mostrarRegistroCliente | Agrega un nuevo cliente al modelo")
  public void mostrarRegistroCliente() {
    String vista = controlador.mostrarRegistroCliente(model);
    assertEquals("portalCliente/registro", vista);
    assertEquals(Cliente.class, model.getAttribute("cliente").getClass());
  }

  @Test
  @DisplayName("procesarRegistroCliente | Si falla el servicio, vuelve a registro con error")
  public void procesarRegistroClienteConError() throws Exception {
    Cliente cliente = new Cliente();
    doThrow(new Exception("Error al registrar")).when(servicioCliente).registrarCliente(cliente);

    MockHttpServletRequest request = new MockHttpServletRequest();
    String vista = controlador.procesarRegistroCliente(cliente, model, request);

    assertEquals("portalCliente/registro", vista);
    assertEquals("Error al registrar", model.getAttribute("error"));
  }

  @Test
  @DisplayName("procesarRegistroCliente | Si exitoso, autentica y redirige a mis pedidos")
  public void procesarRegistroClienteExitoso() throws Exception {
    Cliente cliente = new Cliente(1L, "Juan", "30123456", "1144556677", "juan@test.com", "pass");
    MockHttpServletRequest request = new MockHttpServletRequest();

    String vista = controlador.procesarRegistroCliente(cliente, model, request);

    assertEquals("redirect:/portal/clientes/home", vista);
  }

  @Test
  @DisplayName(
    "mostrarCompletarDatosCliente | Redirige si cliente es null o ya tiene datos completos"
  )
  public void mostrarCompletarDatosClienteRedirecciones() {
    assertEquals(
      "redirect:/portal/clientes",
      controlador.mostrarCompletarDatosCliente(null, model)
    );

    Cliente clienteCompleto = new Cliente(
      1L,
      "Juan",
      "30123456",
      "1144556677",
      "juan@test.com",
      "pass"
    );
    Authentication authCompleto = new UsernamePasswordAuthenticationToken(clienteCompleto, "");
    assertEquals(
      "redirect:/portal/clientes/home",
      controlador.mostrarCompletarDatosCliente(authCompleto, model)
    );
  }

  @Test
  @DisplayName(
    "mostrarCompletarDatosCliente | Devuelve vista completar-datos si faltan DNI o telefono"
  )
  public void mostrarCompletarDatosClienteVista() {
    Cliente clienteIncompleto = new Cliente(1L, "Juan", null, null, "juan@test.com", null);
    Authentication auth = new UsernamePasswordAuthenticationToken(clienteIncompleto, "");

    String vista = controlador.mostrarCompletarDatosCliente(auth, model);

    assertEquals("portalCliente/completar-datos", vista);
    assertEquals(clienteIncompleto, model.getAttribute("cliente"));
  }

  @Test
  @DisplayName(
    "procesarCompletarDatosCliente | Valida datos y redirige a mis pedidos si todo es exitoso"
  )
  public void procesarCompletarDatosClienteExitoso() throws Exception {
    Cliente cliente = new Cliente(1L, "Juan", null, null, "juan@test.com", null);
    Authentication auth = new UsernamePasswordAuthenticationToken(
      cliente,
      "",
      Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
    );
    MockHttpServletRequest request = new MockHttpServletRequest();

    String vista = controlador.procesarCompletarDatosCliente(
      "30111222",
      "1144556677",
      auth,
      model,
      request
    );

    assertEquals("redirect:/portal/clientes/home", vista);
    verify(servicioCliente)
      .actualizarDatosCliente(eq(cliente), eq("30111222"), eq("1144556677"), eq("Juan"));
  }

  @Test
  @DisplayName(
    "procesarCompletarDatosCliente | Devuelve vista con error si falta documento o telefono"
  )
  public void procesarCompletarDatosClienteConError() {
    Cliente cliente = new Cliente(1L, "Juan", null, null, "juan@test.com", null);
    Authentication auth = new UsernamePasswordAuthenticationToken(cliente, "");
    MockHttpServletRequest request = new MockHttpServletRequest();

    String vista = controlador.procesarCompletarDatosCliente(
      "",
      "1144556677",
      auth,
      model,
      request
    );

    assertEquals("portalCliente/completar-datos", vista);
    assertEquals(
      "Por favor, ingresa tanto tu número de DNI como tu teléfono celular.",
      model.getAttribute("error")
    );
  }

  @Test
  @DisplayName("mostrarHome | Redirige a completar-datos si faltan datos obligatorios")
  public void mostrarHomeRedireccion() {
    Cliente clienteIncompleto = new Cliente(1L, "Juan", null, null, "juan@test.com", null);
    Authentication auth = new UsernamePasswordAuthenticationToken(clienteIncompleto, "");

    String vista = controlador.mostrarHome(auth, model);

    assertEquals("redirect:/portal/clientes/completar-datos", vista);
  }

  @Test
  @DisplayName("mostrarHome | Muestra home si datos estan completos")
  public void mostrarHomeVista() {
    Cliente clienteCompleto = new Cliente(
      1L,
      "Juan",
      "30123456",
      "1144556677",
      "juan@test.com",
      "pass"
    );
    Authentication auth = new UsernamePasswordAuthenticationToken(clienteCompleto, "");

    String vista = controlador.mostrarHome(auth, model);

    assertEquals("portalCliente/home", vista);
    assertEquals(false, model.getAttribute("faltanDatos"));
  }

  @Test
  @DisplayName("mostrarHistorialPedidos | Redirige segun estado del cliente o devuelve historial")
  public void mostrarHistorialPedidos() {
    assertEquals("redirect:/portal/clientes", controlador.mostrarHistorialPedidos(null, model));

    Cliente clienteIncompleto = new Cliente(1L, "Juan", null, null, "juan@test.com", null);
    Authentication authIncompleto = new UsernamePasswordAuthenticationToken(clienteIncompleto, "");
    assertEquals(
      "redirect:/portal/clientes/completar-datos",
      controlador.mostrarHistorialPedidos(authIncompleto, model)
    );

    Cliente clienteCompleto = new Cliente(
      1L,
      "Juan",
      "30123456",
      "1144556677",
      "juan@test.com",
      "pass"
    );
    Authentication authCompleto = new UsernamePasswordAuthenticationToken(clienteCompleto, "");
    assertEquals(
      "portalCliente/historial",
      controlador.mostrarHistorialPedidos(authCompleto, model)
    );
  }

  @Test
  @DisplayName("mostrarReportarPedido | Redirige segun estado del cliente o devuelve reportar")
  public void mostrarReportarPedido() {
    assertEquals("redirect:/portal/clientes", controlador.mostrarReportarPedido(null, model));

    Cliente clienteIncompleto = new Cliente(1L, "Juan", null, null, "juan@test.com", null);
    Authentication authIncompleto = new UsernamePasswordAuthenticationToken(clienteIncompleto, "");
    assertEquals(
      "redirect:/portal/clientes/completar-datos",
      controlador.mostrarReportarPedido(authIncompleto, model)
    );

    Cliente clienteCompleto = new Cliente(
      1L,
      "Juan",
      "30123456",
      "1144556677",
      "juan@test.com",
      "pass"
    );
    Authentication authCompleto = new UsernamePasswordAuthenticationToken(clienteCompleto, "");
    assertEquals("portalCliente/reportar", controlador.mostrarReportarPedido(authCompleto, model));
  }

  @Test
  @DisplayName("mostrarPerfilCliente | Redirige si no hay sesion o muestra perfil")
  public void mostrarPerfilCliente() {
    assertEquals("redirect:/portal/clientes", controlador.mostrarPerfilCliente(null, model));

    Cliente cliente = new Cliente(1L, "Juan", "30123456", "1144556677", "juan@test.com", "pass");
    Authentication auth = new UsernamePasswordAuthenticationToken(cliente, "");
    assertEquals("portalCliente/perfil", controlador.mostrarPerfilCliente(auth, model));
  }

  @Test
  @DisplayName(
    "guardarPerfilCliente | Redirige si no hay sesion, guarda exitosamente o maneja error"
  )
  public void guardarPerfilCliente() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    assertEquals(
      "redirect:/portal/clientes",
      controlador.guardarPerfilCliente("12345678", "1122334455", "Juan", null, model, request)
    );

    Cliente cliente = new Cliente(1L, "Juan", "30123456", "1144556677", "juan@test.com", "pass");
    Authentication auth = new UsernamePasswordAuthenticationToken(cliente, "");

    String vistaExito = controlador.guardarPerfilCliente(
      "33445566",
      "1199887766",
      "Juan Modificado",
      auth,
      model,
      request
    );
    assertEquals("portalCliente/perfil", vistaExito);
    assertEquals("¡Tus datos han sido actualizados correctamente!", model.getAttribute("exito"));
    verify(servicioCliente)
      .actualizarDatosCliente(eq(cliente), eq("33445566"), eq("1199887766"), eq("Juan Modificado"));

    doThrow(new Exception("Error al actualizar"))
      .when(servicioCliente)
      .actualizarDatosCliente(any(), anyString(), anyString(), anyString());
    String vistaError = controlador.guardarPerfilCliente(
      "33445566",
      "1199887766",
      "Juan Modificado",
      auth,
      model,
      request
    );
    assertEquals("portalCliente/perfil", vistaError);
    assertEquals("Error al actualizar", model.getAttribute("error"));
  }
}
