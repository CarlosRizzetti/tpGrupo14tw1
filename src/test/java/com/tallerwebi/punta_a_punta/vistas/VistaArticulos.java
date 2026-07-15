package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaArticulos extends VistaWeb {

  public VistaArticulos(Page page) {
    super(page);
    page.navigate("localhost:8080/admin/articulos");
  }

  // articulos.js pone data-numero-lote en cada <tr class="articulo-row">.
  public boolean existeLoteConNumero(String numeroDeLote) {
    return page.locator("tr.articulo-row[data-numero-lote='" + numeroDeLote + "']").count() > 0;
  }

  public String obtenerNombreProductoDelLote(String numeroDeLote) {
    return this.obtenerTextoDelElemento(
        "tr.articulo-row[data-numero-lote='" + numeroDeLote + "'] .articulo-nombre"
      );
  }

  // Dispara el mismo click delegado que ya usa articulos.js para navegar
  // al detalle del lote.
  public void hacerClickEnLote(String numeroDeLote) {
    page.locator("tr.articulo-row[data-numero-lote='" + numeroDeLote + "']").click();
  }
}
