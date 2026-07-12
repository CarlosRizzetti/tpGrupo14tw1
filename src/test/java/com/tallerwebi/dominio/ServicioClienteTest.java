package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
}
