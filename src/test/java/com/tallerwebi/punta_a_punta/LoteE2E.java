package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.tallerwebi.punta_a_punta.vistas.VistaArticulos;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaNuevoLote;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoteE2E {

  // Producto sembrado en import.sql: id 5 = "Queso".
  private static final String ID_PRODUCTO_QUESO = "5";
  private static final String NOMBRE_PRODUCTO_QUESO = "Queso";

  // Único por corrida: ReiniciarDB solo resetea Usuario, no Lote, así que un
  // valor fijo terminaría duplicado (y rompería el locator en modo estricto)
  // si el test corre más de una vez contra la misma base.
  private static final String NUMERO_DE_LOTE_NUEVO = String.valueOf(System.currentTimeMillis());

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  Page page;
  VistaLogin vistaLogin;
  VistaNuevoLote vistaNuevoLote;

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
  void deberiaRegistrarUnLoteYVerloEnElListado() throws MalformedURLException {
    dadoQueElUsuarioCompletaElFormularioDeNuevoLote();
    cuandoElUsuarioGuardaElLote();
    entoncesDeberiaSerRedirigidoAlPanelDeAdmin();
    entoncesElLoteDeberiaAparecerEnElListadoConSuProducto();
  }

  private void dadoQueElUsuarioIniciaSesionComoAdmin() {
    vistaLogin.escribirEMAIL("test@unlam.edu.ar");
    vistaLogin.escribirClave("Unlam2026");
    vistaLogin.marcarRecaptcha();
    vistaLogin.darClickEnIniciarSesion();
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  private void dadoQueElUsuarioCompletaElFormularioDeNuevoLote() {
    vistaNuevoLote = new VistaNuevoLote(context.pages().get(0));
    vistaNuevoLote.seleccionarProducto(ID_PRODUCTO_QUESO);
    vistaNuevoLote.escribirMarca("Marca Test");
    vistaNuevoLote.escribirProveedor("Proveedor Test");
    vistaNuevoLote.escribirNumeroDeLote(NUMERO_DE_LOTE_NUEVO);
    vistaNuevoLote.escribirCantidadInicial("15");
    vistaNuevoLote.escribirFechaDeIngreso("2026-07-14T10:00");
    vistaNuevoLote.escribirFechaDeVencimiento("2026-12-31T10:00");
  }

  private void cuandoElUsuarioGuardaElLote() {
    vistaNuevoLote.darClickEnGuardar();
  }

  private void entoncesDeberiaSerRedirigidoAlPanelDeAdmin() throws MalformedURLException {
    URL url = vistaNuevoLote.obtenerURLActual();
    assertThat(url.getPath(), matchesPattern("^/admin(?:;jsessionid=[^/\\s]+)?$"));
  }

  private void entoncesElLoteDeberiaAparecerEnElListadoConSuProducto() {
    VistaArticulos vistaArticulos = new VistaArticulos(context.pages().get(0));
    assertThat(vistaArticulos.existeLoteConNumero(NUMERO_DE_LOTE_NUEVO), is(true));
    assertThat(
      vistaArticulos.obtenerNombreProductoDelLote(NUMERO_DE_LOTE_NUEVO),
      equalToIgnoringCase(NOMBRE_PRODUCTO_QUESO)
    );
  }
}
