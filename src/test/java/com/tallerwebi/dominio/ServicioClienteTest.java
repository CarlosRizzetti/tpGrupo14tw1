package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.RepositorioCliente;
import com.tallerwebi.dominio.services.ServicioClienteImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServicioClienteTest {

  private RepositorioCliente repositorioCliente;
  private ServicioClienteImpl servicio;

  @BeforeEach
  public void init() {
    repositorioCliente = mock(RepositorioCliente.class);
    servicio = new ServicioClienteImpl(repositorioCliente);
  }

  @Test
  @DisplayName(
    "NEG-01 | buscarPorDocumento | Devuelve null sin consultar el repositorio si el documento es null"
  )
  public void buscarPorDocumentoNuloDeberiaDevolverNullSinConsultarElRepositorio() {
    Cliente resultado = servicio.buscarPorDocumento(null);

    assertNull(resultado);
    verifyNoInteractions(repositorioCliente);
  }

  @Test
  @DisplayName(
    "EDGE-01 | buscarPorDocumento | Devuelve null sin consultar el repositorio si el documento es vacío"
  )
  public void buscarPorDocumentoVacioDeberiaDevolverNullSinConsultarElRepositorio() {
    Cliente resultado = servicio.buscarPorDocumento("");

    assertNull(resultado);
    verifyNoInteractions(repositorioCliente);
  }

  @Test
  @DisplayName(
    "EDGE-02 | buscarPorDocumento | Devuelve null sin consultar el repositorio si el documento es solo espacios"
  )
  public void buscarPorDocumentoConSoloEspaciosDeberiaDevolverNullSinConsultarElRepositorio() {
    Cliente resultado = servicio.buscarPorDocumento("   ");

    assertNull(resultado);
    verifyNoInteractions(repositorioCliente);
  }

  @Test
  @DisplayName(
    "HP-01 | buscarPorDocumento | Delega en el repositorio con el documento recortado (trim)"
  )
  public void buscarPorDocumentoValidoDeberiaDelegarEnElRepositorioConElDocumentoRecortado() {
    Cliente cliente = new Cliente();
    when(repositorioCliente.buscarPorDocumento("30123456")).thenReturn(cliente);

    Cliente resultado = servicio.buscarPorDocumento("  30123456  ");

    assertEquals(cliente, resultado);
    verify(repositorioCliente, times(1)).buscarPorDocumento("30123456");
  }

  @Test
  @DisplayName(
    "EDGE-03 | buscarPorDocumento | Devuelve null si el repositorio no encuentra el cliente"
  )
  public void buscarPorDocumentoSinCoincidenciaDeberiaDevolverNull() {
    when(repositorioCliente.buscarPorDocumento("99999999")).thenReturn(null);

    Cliente resultado = servicio.buscarPorDocumento("99999999");

    assertNull(resultado);
    verify(repositorioCliente, times(1)).buscarPorDocumento("99999999");
  }

  @Test
  @DisplayName(
    "HP-02 | buscarPorEmail | Delega en el repositorio con email en minusculas y recortado"
  )
  public void buscarPorEmailValidoDeberiaDelegarEnRepositorio() {
    Cliente cliente = new Cliente();
    when(repositorioCliente.buscarPorEmail("test@test.com")).thenReturn(cliente);

    Cliente resultado = servicio.buscarPorEmail("  Test@Test.COM  ");

    assertEquals(cliente, resultado);
    verify(repositorioCliente, times(1)).buscarPorEmail("test@test.com");
  }

  @Test
  @DisplayName("NEG-02 | buscarPorEmail | Devuelve null sin consultar si el email es null o vacio")
  public void buscarPorEmailNuloOVacioDeberiaDevolverNull() {
    assertNull(servicio.buscarPorEmail(null));
    assertNull(servicio.buscarPorEmail("   "));
    verifyNoInteractions(repositorioCliente);
  }

  @Test
  @DisplayName("NEG-03 | registrarCliente | Lanza excepcion si el documento es nulo o vacio")
  public void registrarClienteSinDocumentoDeberiaLanzarExcepcion() {
    Cliente cliente = new Cliente(1L, "Nombre", null, "1144556677", "test@test.com", "pass");

    Exception ex = assertThrows(Exception.class, () -> servicio.registrarCliente(cliente));
    assertEquals("El número de documento (DNI) y el teléfono son obligatorios.", ex.getMessage());
  }

  @Test
  @DisplayName("NEG-04 | registrarCliente | Lanza excepcion si el telefono es nulo o vacio")
  public void registrarClienteSinTelefonoDeberiaLanzarExcepcion() {
    Cliente cliente = new Cliente(1L, "Nombre", "30123456", "  ", "test@test.com", "pass");

    Exception ex = assertThrows(Exception.class, () -> servicio.registrarCliente(cliente));
    assertEquals("El número de documento (DNI) y el teléfono son obligatorios.", ex.getMessage());
  }

  @Test
  @DisplayName(
    "NEG-04B | registrarCliente | Lanza excepcion si el DNI no tiene 8 digitos numéricos"
  )
  public void registrarClienteConDniInvalidoDeberiaLanzarExcepcion() {
    Cliente cliente = new Cliente(1L, "Nombre", "123", "1144556677", "test@test.com", "pass");

    Exception ex = assertThrows(Exception.class, () -> servicio.registrarCliente(cliente));
    assertEquals("El DNI debe tener exactamente 8 dígitos numéricos.", ex.getMessage());
  }

  @Test
  @DisplayName(
    "NEG-04C | registrarCliente | Lanza excepcion si el telefono no tiene 10 digitos numéricos"
  )
  public void registrarClienteConTelefonoInvalidoDeberiaLanzarExcepcion() {
    Cliente cliente = new Cliente(1L, "Nombre", "30123456", "12345", "test@test.com", "pass");

    Exception ex = assertThrows(Exception.class, () -> servicio.registrarCliente(cliente));
    assertEquals("El teléfono debe tener exactamente 10 dígitos numéricos.", ex.getMessage());
  }

  @Test
  @DisplayName("NEG-05 | registrarCliente | Lanza excepcion si el email ya existe")
  public void registrarClienteConEmailDuplicadoDeberiaLanzarExcepcion() {
    Cliente cliente = new Cliente(1L, "Nombre", "30123456", "1144556677", "test@test.com", "pass");
    when(repositorioCliente.buscarPorEmail("test@test.com")).thenReturn(new Cliente());

    Exception ex = assertThrows(Exception.class, () -> servicio.registrarCliente(cliente));
    assertEquals("El correo electrónico ya se encuentra registrado.", ex.getMessage());
  }

  @Test
  @DisplayName("NEG-06 | registrarCliente | Lanza excepcion si el DNI ya existe")
  public void registrarClienteConDniDuplicadoDeberiaLanzarExcepcion() {
    Cliente cliente = new Cliente(1L, "Nombre", "30123456", "1144556677", "test@test.com", "pass");
    when(repositorioCliente.buscarPorEmail("test@test.com")).thenReturn(null);
    when(repositorioCliente.buscarPorDocumento("30123456")).thenReturn(new Cliente());

    Exception ex = assertThrows(Exception.class, () -> servicio.registrarCliente(cliente));
    assertEquals("El número de documento (DNI) ya se encuentra registrado.", ex.getMessage());
  }

  @Test
  @DisplayName("HP-03 | registrarCliente | Registra exitosamente al cliente con datos validos")
  public void registrarClienteExitosamente() throws Exception {
    Cliente cliente = new Cliente(
      1L,
      "Nombre",
      "30123456",
      "1144556677",
      "test@test.com",
      "password123"
    );
    when(repositorioCliente.buscarPorEmail("test@test.com")).thenReturn(null);
    when(repositorioCliente.buscarPorDocumento("30123456")).thenReturn(null);

    servicio.registrarCliente(cliente);

    verify(repositorioCliente, times(1)).guardar(cliente);
  }

  @Test
  @DisplayName("HP-04 | guardar | Delega al repositorio")
  public void guardarDeberiaDelegarAlRepositorio() {
    Cliente cliente = new Cliente();
    servicio.guardar(cliente);
    verify(repositorioCliente, times(1)).guardar(cliente);
  }

  @Test
  @DisplayName("NEG-07 | actualizarDatosCliente | Lanza excepcion si falta documento o telefono")
  public void actualizarDatosClienteSinDatosDeberiaLanzarExcepcion() {
    Cliente cliente = new Cliente(1L, "Nombre", "30123456", "1144556677", "test@test.com", "pass");

    assertThrows(
      Exception.class,
      () -> servicio.actualizarDatosCliente(cliente, null, "1144556677", "Nombre")
    );
    assertThrows(
      Exception.class,
      () -> servicio.actualizarDatosCliente(cliente, "30123456", "   ", "Nombre")
    );
  }

  @Test
  @DisplayName(
    "NEG-08 | actualizarDatosCliente | Lanza excepcion si el documento esta registrado por otro cliente"
  )
  public void actualizarDatosClienteConDniDeOtroClienteDeberiaLanzarExcepcion() {
    Cliente cliente = new Cliente(1L, "Nombre", "30123456", "1144556677", "test@test.com", "pass");
    Cliente otroCliente = new Cliente(
      2L,
      "Otro",
      "30999999",
      "1144556688",
      "otro@test.com",
      "pass"
    );
    when(repositorioCliente.buscarPorDocumento("30999999")).thenReturn(otroCliente);

    Exception ex = assertThrows(
      Exception.class,
      () -> servicio.actualizarDatosCliente(cliente, "30999999", "1144556677", "Nombre")
    );
    assertEquals(
      "El número de documento (DNI) ya se encuentra registrado por otra cuenta.",
      ex.getMessage()
    );
  }

  @Test
  @DisplayName(
    "HP-05 | actualizarDatosCliente | Actualiza exitosamente documento, telefono y nombre"
  )
  public void actualizarDatosClienteExitosamente() throws Exception {
    Cliente cliente = new Cliente(
      1L,
      "Nombre Viejo",
      "30123456",
      "1144556677",
      "test@test.com",
      "pass"
    );
    when(repositorioCliente.buscarPorDocumento("30999999")).thenReturn(null);

    servicio.actualizarDatosCliente(cliente, "30999999", "1155667788", "Nombre Nuevo");

    assertEquals("30999999", cliente.getDocumento());
    assertEquals("1155667788", cliente.getTelefono());
    assertEquals("Nombre Nuevo", cliente.getNombre());
    verify(repositorioCliente, times(1)).guardar(cliente);
  }
}
