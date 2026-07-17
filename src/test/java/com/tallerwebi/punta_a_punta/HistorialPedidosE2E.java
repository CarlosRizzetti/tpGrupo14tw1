package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.tallerwebi.punta_a_punta.vistas.VistaCaja;
import com.tallerwebi.punta_a_punta.vistas.VistaCobro;
import com.tallerwebi.punta_a_punta.vistas.VistaComandas;
import com.tallerwebi.punta_a_punta.vistas.VistaHistorialPedidos;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HistorialPedidosE2E {

  // Confirmado que tiene timers activos con stock (Hamburguesa, Queso,
  // Cebolla, Tomate) para poder sacar la comanda sin el 409 de faltantes.
  private static final String NOMBRE_PRODUCTO = "Hamburguesa Completa";

  static Playwright playwright;
  static Browser browser;

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
  void limpiarBaseDeDatos() {
    ReiniciarDB.limpiarBaseDeDatos();
  }

  // Este test necesita DOS sesiones distintas (el cajero que arma y cobra el
  // pedido no tiene permiso para ver el historial ni servir comandas, y el
  // admin no tiene permiso para entrar a /cajero), así que no reutiliza el
  // patrón de un único "context" de instancia como los otros dos E2E: cada
  // mitad del flujo abre y cierra su propio BrowserContext.
  @Test
  void deberiaVerElPedidoEnElHistorialAlFiltrarPorFechaLuegoDeCobrarloYServirlo() {
    long idPedido = dadoQueSeArmaYCobraUnPedidoComoCajero();
    cuandoSeSirveLaComandaYSeBuscaEnElHistorialComoAdmin(idPedido);
  }

  private long dadoQueSeArmaYCobraUnPedidoComoCajero() {
    BrowserContext contextoCajero = browser.newContext();
    Page pageCajero = contextoCajero.newPage();

    VistaLogin vistaLogin = new VistaLogin(pageCajero);
    vistaLogin.escribirEMAIL("cajero@unlam.edu.ar");
    vistaLogin.escribirClave("Unlam2026");
    vistaLogin.marcarRecaptcha();
    vistaLogin.darClickEnIniciarSesion();
    pageCajero.waitForLoadState(LoadState.NETWORKIDLE);

    VistaCaja vistaCaja = new VistaCaja(pageCajero);
    vistaCaja.seleccionarProducto(NOMBRE_PRODUCTO);
    vistaCaja.confirmarAgregarAlCarritoSiHaceFalta();
    vistaCaja.irACobrar();

    VistaCobro vistaCobro = new VistaCobro(pageCajero);
    long idPedido = vistaCobro.confirmarCobroYObtenerIdPedido();

    contextoCajero.close();
    return idPedido;
  }

  private void cuandoSeSirveLaComandaYSeBuscaEnElHistorialComoAdmin(long idPedido) {
    BrowserContext contextoAdmin = browser.newContext();
    Page pageAdmin = contextoAdmin.newPage();

    VistaLogin vistaLogin = new VistaLogin(pageAdmin);
    vistaLogin.escribirEMAIL("test@unlam.edu.ar");
    vistaLogin.escribirClave("Unlam2026");
    vistaLogin.marcarRecaptcha();
    vistaLogin.darClickEnIniciarSesion();
    pageAdmin.waitForLoadState(LoadState.NETWORKIDLE);

    VistaComandas vistaComandas = new VistaComandas(pageAdmin);
    vistaComandas.servirComanda(idPedido);

    String hoy = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    VistaHistorialPedidos vistaHistorial = new VistaHistorialPedidos(pageAdmin);
    vistaHistorial.filtrarPorFecha(hoy);
    vistaHistorial.esperarPedido(idPedido);

    contextoAdmin.close();
  }
}
