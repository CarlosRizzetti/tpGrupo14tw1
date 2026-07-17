package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaHistorialPedidos extends VistaWeb {

  public VistaHistorialPedidos(Page page) {
    super(page);
    page.navigate("localhost:8080/admin/historial-pedidos");
  }

  // desde/hasta son <input type="date">, se llenan con fill() igual que las
  // fechas de VistaNuevoLote.
  public void filtrarPorFecha(String fechaIsoYyyyMmDd) {
    page.locator("#desde").fill(fechaIsoYyyyMmDd);
    page.locator("#hasta").fill(fechaIsoYyyyMmDd);
    page.locator("#filtros-historial button[type='submit']").click();
  }

  // El resultado se arma por AJAX (fetch a /admin/historial-pedidos/buscar),
  // así que hay que esperar a que aparezca en vez de comprobar el estado
  // actual: el locator con auto-wait de Playwright reintenta solo hasta que
  // el texto aparece o se cumple el timeout.
  public void esperarPedido(long idPedido) {
    page.locator("#resultados-pedidos").getByText("Pedido #" + idPedido).first().waitFor();
  }
}
