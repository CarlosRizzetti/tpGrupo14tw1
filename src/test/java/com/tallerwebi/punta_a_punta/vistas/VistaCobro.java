package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VistaCobro extends VistaWeb {

  public VistaCobro(Page page) {
    super(page);
  }

  public long confirmarCobroYObtenerIdPedido() {
    page.locator("[data-monto-exacto]").click();

    CompletableFuture<String> cuerpoRespuesta = new CompletableFuture<>();
    page.route(
      "**/api/cajero/cobrar*",
      route -> {
        APIResponse respuestaReal = route.fetch();
        cuerpoRespuesta.complete(respuestaReal.text());
        route.fulfill(new Route.FulfillOptions().setResponse(respuestaReal));
      }
    );

    page.locator("[data-btn-cobrar-efectivo]").click();

    String cuerpo;
    try {
      cuerpo = cuerpoRespuesta.get(15, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new RuntimeException("No se recibió respuesta de /api/cajero/cobrar a tiempo", e);
    } finally {
      page.unroute("**/api/cajero/cobrar*");
    }

    Matcher matcher = Pattern.compile("\"idPedido\"\\s*:\\s*(\\d+)").matcher(cuerpo);
    if (!matcher.find()) {
      throw new RuntimeException(
        "No se encontró idPedido en la respuesta de /api/cajero/cobrar: " + cuerpo
      );
    }
    return Long.parseLong(matcher.group(1));
  }
}
