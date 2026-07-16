package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.entity.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClienteTest {

  @Test
  @DisplayName("HP-01 | toString | Devuelve el email cuando el cliente tiene uno cargado")
  void toStringDeberiaDevolverElEmailCuandoExiste() {
    Cliente cliente = crearCliente("Juan Pérez", "40123456", "juan@mail.com");

    assertEquals("juan@mail.com", cliente.toString());
  }

  @Test
  @DisplayName("HP-02 | toString | Devuelve el documento cuando no hay email pero sí documento")
  void toStringDeberiaDevolverElDocumentoCuandoNoHayEmail() {
    Cliente cliente = crearCliente("Juan Pérez", "40123456", null);

    assertEquals("40123456", cliente.toString());
  }

  @Test
  @DisplayName("HP-03 | toString | Devuelve el nombre cuando no hay email ni documento")
  void toStringDeberiaDevolverElNombreCuandoNoHayEmailNiDocumento() {
    Cliente cliente = crearCliente("Juan Pérez", null, null);

    assertEquals("Juan Pérez", cliente.toString());
  }

  @Test
  @DisplayName("HP-04 | toString | Prioriza el email por sobre el documento cuando tiene ambos")
  void toStringDeberiaPriorizarElEmailPorSobreElDocumento() {
    Cliente cliente = crearCliente("Juan Pérez", "40123456", "juan@mail.com");

    assertEquals("juan@mail.com", cliente.toString());
  }

  @Test
  @DisplayName(
    "EDGE-01 | toString | Devuelve \"Cliente\" cuando no tiene email, documento ni nombre"
  )
  void toStringDeberiaDevolverClienteCuandoNoHayNingunDato() {
    Cliente cliente = crearCliente(null, null, null);

    assertEquals("Cliente", cliente.toString());
  }

  @Test
  @DisplayName("EDGE-02 | toString | Trata el email en blanco igual que un email null")
  void toStringDeberiaTratarElEmailEnBlancoComoNull() {
    Cliente cliente = crearCliente("Juan Pérez", "40123456", "   ");

    assertEquals("40123456", cliente.toString());
  }

  @Test
  @DisplayName("EDGE-03 | toString | Trata el documento en blanco igual que un documento null")
  void toStringDeberiaTratarElDocumentoEnBlancoComoNull() {
    Cliente cliente = crearCliente("Juan Pérez", "   ", null);

    assertEquals("Juan Pérez", cliente.toString());
  }

  private Cliente crearCliente(String nombre, String documento, String email) {
    Cliente cliente = new Cliente();
    cliente.setNombre(nombre);
    cliente.setDocumento(documento);
    cliente.setEmail(email);
    return cliente;
  }
}
