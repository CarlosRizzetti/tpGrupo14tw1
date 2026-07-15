package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaLogin extends VistaWeb {

  public VistaLogin(Page page) {
    super(page);
    page.navigate("localhost:8080/");
  }

  public String obtenerTextoDeLaBarraDeNavegacion() {
    return this.obtenerTextoDelElemento("nav a.navbar-brand");
  }

  public String obtenerMensajeDeError() {
    return this.obtenerTextoDelElemento("p.alert.alert-danger");
  }

  public void escribirEMAIL(String email) {
    this.escribirEnElElemento("#email", email);
  }

  public void escribirClave(String clave) {
    this.escribirEnElElemento("#password", clave);
  }

  public void darClickEnIniciarSesion() {
    this.darClickEnElElemento("#btn-login");
  }

  public void darClickEnRegistrarse() {
    this.darClickEnElElemento("#btn-register");
  }

  public void marcarRecaptcha() {
    page.frameLocator("iframe[title='reCAPTCHA']").locator("#recaptcha-anchor").click();
    page.waitForFunction(
      "() => document.getElementById('g-recaptcha-response')?.value?.length > 0"
    );
  }
}
