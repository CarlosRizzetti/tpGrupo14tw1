package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.entity.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PedidoTest {

  // ========================================================
  // getReportado / setReportado
  // ========================================================

  @Test
  @DisplayName("HP-01 | getReportado | Devuelve false por defecto en un pedido recién creado")
  void getReportadoDeberiaDevolverFalsePorDefecto() {
    Pedido pedido = new Pedido();

    assertFalse(pedido.getReportado());
  }

  @Test
  @DisplayName("HP-02 | setReportado/getReportado | Persiste y devuelve el valor seteado")
  void setReportadoDeberiaPersistirElValor() {
    Pedido pedido = new Pedido();

    pedido.setReportado(true);

    assertTrue(pedido.getReportado());
  }

  @Test
  @DisplayName(
    "EDGE-01 | setReportado | Crea un detalleReclamo nuevo si estaba en null y persiste el valor"
  )
  void setReportadoDeberiaCrearDetalleReclamoSiEraNull() {
    Pedido pedido = new Pedido();
    pedido.setDetalleReclamo(null);

    pedido.setReportado(true);

    assertTrue(pedido.getReportado());
  }

  @Test
  @DisplayName(
    "EDGE-02 | getReportado | Devuelve false (no null ni NPE) cuando detalleReclamo es null"
  )
  void getReportadoDeberiaDevolverFalseCuandoDetalleReclamoEsNull() {
    Pedido pedido = new Pedido();
    pedido.setDetalleReclamo(null);

    assertFalse(pedido.getReportado());
  }

  // ========================================================
  // getMotivoReclamo / setMotivoReclamo
  // ========================================================

  @Test
  @DisplayName("HP-03 | setMotivoReclamo/getMotivoReclamo | Persiste y devuelve el valor seteado")
  void setMotivoReclamoDeberiaPersistirElValor() {
    Pedido pedido = new Pedido();

    pedido.setMotivoReclamo("Producto en mal estado");

    assertEquals("Producto en mal estado", pedido.getMotivoReclamo());
  }

  @Test
  @DisplayName(
    "EDGE-03 | setMotivoReclamo | Crea un detalleReclamo nuevo si estaba en null y persiste el valor"
  )
  void setMotivoReclamoDeberiaCrearDetalleReclamoSiEraNull() {
    Pedido pedido = new Pedido();
    pedido.setDetalleReclamo(null);

    pedido.setMotivoReclamo("Producto en mal estado");

    assertEquals("Producto en mal estado", pedido.getMotivoReclamo());
  }

  @Test
  @DisplayName("EDGE-04 | getMotivoReclamo | Devuelve null cuando detalleReclamo es null")
  void getMotivoReclamoDeberiaDevolverNullCuandoDetalleReclamoEsNull() {
    Pedido pedido = new Pedido();
    pedido.setDetalleReclamo(null);

    assertNull(pedido.getMotivoReclamo());
  }

  // ========================================================
  // getComentarioReclamo / setComentarioReclamo
  // ========================================================

  @Test
  @DisplayName(
    "HP-04 | setComentarioReclamo/getComentarioReclamo | Persiste y devuelve el valor seteado"
  )
  void setComentarioReclamoDeberiaPersistirElValor() {
    Pedido pedido = new Pedido();

    pedido.setComentarioReclamo("El cliente pidió reembolso");

    assertEquals("El cliente pidió reembolso", pedido.getComentarioReclamo());
  }

  @Test
  @DisplayName(
    "EDGE-05 | setComentarioReclamo | Crea un detalleReclamo nuevo si estaba en null y persiste el valor"
  )
  void setComentarioReclamoDeberiaCrearDetalleReclamoSiEraNull() {
    Pedido pedido = new Pedido();
    pedido.setDetalleReclamo(null);

    pedido.setComentarioReclamo("El cliente pidió reembolso");

    assertEquals("El cliente pidió reembolso", pedido.getComentarioReclamo());
  }

  @Test
  @DisplayName("EDGE-06 | getComentarioReclamo | Devuelve null cuando detalleReclamo es null")
  void getComentarioReclamoDeberiaDevolverNullCuandoDetalleReclamoEsNull() {
    Pedido pedido = new Pedido();
    pedido.setDetalleReclamo(null);

    assertNull(pedido.getComentarioReclamo());
  }

  // ========================================================
  // Independencia entre campos
  // ========================================================

  @Test
  @DisplayName("NEG-01 | setReportado | No pisa motivoReclamo ni comentarioReclamo ya cargados")
  void setReportadoNoDeberiaPisarOtrosCamposDelReclamo() {
    Pedido pedido = new Pedido();
    pedido.setMotivoReclamo("Producto en mal estado");
    pedido.setComentarioReclamo("El cliente pidió reembolso");

    pedido.setReportado(true);

    assertTrue(pedido.getReportado());
    assertEquals("Producto en mal estado", pedido.getMotivoReclamo());
    assertEquals("El cliente pidió reembolso", pedido.getComentarioReclamo());
  }
}
