package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;
import java.util.stream.Collectors;

public class VistaComandas extends VistaWeb {

  public VistaComandas(Page page) {
    super(page);
    page.navigate("localhost:8080/cocina/comandas");
  }

  // Recorre las categorías de comandas hasta encontrar la que tiene el
  // pedido buscado (no hay forma de saber de antemano a qué categoría
  // pertenece sin mirar), y ahí clickea "Servido" en su card.
  public void servirComanda(long idPedido) {
    List<String> hrefs = page
      .locator("a[href*='/cocina/comandas/categoria/']")
      .all()
      .stream()
      .map(locator -> locator.getAttribute("href"))
      .collect(Collectors.toList());

    for (String href : hrefs) {
      page.navigate("http://localhost:8080" + href);
      page.waitForLoadState(LoadState.NETWORKIDLE);
      Locator card = page.locator("article", new Page.LocatorOptions().setHasText("#" + idPedido));
      if (card.count() > 0) {
        card.locator("button", new Locator.LocatorOptions().setHasText("Servido")).click();
        // Al servirse, comandas.js re-consulta las pendientes y la card
        // desaparece del listado - confirma que se sirvió correctamente.
        card.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
        return;
      }
    }
    throw new RuntimeException(
      "No se encontró el pedido #" + idPedido + " en ninguna categoría de comandas"
    );
  }
}
