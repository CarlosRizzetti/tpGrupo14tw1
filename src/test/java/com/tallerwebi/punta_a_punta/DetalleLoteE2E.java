package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.matchesPattern;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.tallerwebi.punta_a_punta.vistas.VistaArticulos;
import com.tallerwebi.punta_a_punta.vistas.VistaDetalleLote;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DetalleLoteE2E {

  // Lote sembrado en import.sql: id 1, numeroDeLote 1001, producto "Queso" (id 5).
  private static final String NUMERO_DE_LOTE_QUESO = "1001";
  private static final String ID_LOTE_QUESO = "1";

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  Page page;
  VistaLogin vistaLogin;
  VistaArticulos vistaArticulos;
  VistaDetalleLote vistaDetalleLote;

  @BeforeAll
  static void abrirNavegador() {
    playwright = Playwright.create();
    browser =
      playwright
        .chromium()
        .launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
  }

  @AfterAll
  static void cerrarNavegador() {
    playwright.close();
  }

  @BeforeEach
  void crearContextoYPagina() {
    ReiniciarDB.limpiarBaseDeDatos();
    context = browser.newContext();
    page = context.newPage();
    vistaLogin = new VistaLogin(page);
    dadoQueElUsuarioIniciaSesionComoAdmin();
  }

  @AfterEach
  void cerrarContexto() {
    context.close();
  }

  @Test
  void deberiaMostrarElDetalleDelLoteAlHacerClickDesdeElListado() throws MalformedURLException {
    dadoQueElUsuarioEstaEnElListadoDeLotes();
    cuandoElUsuarioHaceClickEnUnLote();
    entoncesDeberiaSerRedirigidoAlDetalleDeEseLote();
    entoncesDeberiaVerLosDatosCorrectosDelLote();
  }

  private void dadoQueElUsuarioIniciaSesionComoAdmin() {
    vistaLogin.escribirEMAIL("test@unlam.edu.ar");
    vistaLogin.escribirClave("Unlam2026");
    vistaLogin.marcarRecaptcha();
    vistaLogin.darClickEnIniciarSesion();
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  private void dadoQueElUsuarioEstaEnElListadoDeLotes() {
    vistaArticulos = new VistaArticulos(context.pages().get(0));
  }

  private void cuandoElUsuarioHaceClickEnUnLote() {
    vistaArticulos.hacerClickEnLote(NUMERO_DE_LOTE_QUESO);
    vistaDetalleLote = new VistaDetalleLote(context.pages().get(0));
  }

  private void entoncesDeberiaSerRedirigidoAlDetalleDeEseLote() throws MalformedURLException {
    URL url = vistaDetalleLote.obtenerURLActual();
    assertThat(
      url.getPath(),
      matchesPattern("^/admin/lotes/" + ID_LOTE_QUESO + "(?:;jsessionid=[^/\\s]+)?$")
    );
  }

  private void entoncesDeberiaVerLosDatosCorrectosDelLote() {
    assertThat(vistaDetalleLote.obtenerNombreProducto(), equalToIgnoringCase("Queso"));
    assertThat(vistaDetalleLote.obtenerNumeroDeLote(), equalToIgnoringCase(NUMERO_DE_LOTE_QUESO));
    assertThat(vistaDetalleLote.obtenerProveedor(), equalToIgnoringCase("Lácteos del Sur"));
    assertThat(vistaDetalleLote.obtenerMarca(), equalToIgnoringCase("La Serenísima"));
  }
}
