package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import java.util.List;

public class VistaCaja extends VistaWeb {

  public VistaCaja(Page page) {
    super(page);
    page.navigate("localhost:8080/cajero");
  }

  public void seleccionarProducto(String nombreProducto) {
    List<Locator> categorias = page.locator("[data-btn-categoria]").all();
    for (Locator categoria : categorias) {
      categoria.click();
      page.waitForLoadState(LoadState.NETWORKIDLE);
      Locator productoBtn = page.locator(
        "[data-grilla-productos] button",
        new Page.LocatorOptions().setHasText(nombreProducto)
      );
      if (productoBtn.count() > 0) {
        productoBtn.first().click();
        return;
      }
    }
    throw new RuntimeException(
      "No se encontró el producto '" + nombreProducto + "' en ninguna categoría"
    );
  }

  public void confirmarAgregarAlCarritoSiHaceFalta() {
    try {
      page.locator("[data-btn-confirmar]").click(new Locator.ClickOptions().setTimeout(2000));
    } catch (TimeoutError e) {}
  }

  public void irACobrar() {
    page.locator("[data-btn-cobrar]").click();
  }
}
