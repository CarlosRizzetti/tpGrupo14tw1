package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaNuevoLote extends VistaWeb {

  public VistaNuevoLote(Page page) {
    super(page);
    page.navigate("localhost:8080/admin/nuevo-articulo");
  }

  public void seleccionarProducto(String idProducto) {
    page.locator("#productoId").selectOption(idProducto);
  }

  public void escribirMarca(String marca) {
    this.escribirEnElElemento("#marca", marca);
  }

  public void escribirProveedor(String proveedor) {
    this.escribirEnElElemento("#proveedor", proveedor);
  }

  public void escribirNumeroDeLote(String numeroDeLote) {
    this.escribirEnElElemento("#numeroDeLote", numeroDeLote);
  }

  public void escribirCantidadInicial(String cantidad) {
    this.escribirEnElElemento("#cantidadInicial", cantidad);
  }

  // Los inputs datetime-local se llenan mejor con fill() que con type(): el
  // widget nativo del navegador no siempre acepta bien las teclas simuladas.
  public void escribirFechaDeIngreso(String fechaIso) {
    page.locator("#fechaDeIngreso").fill(fechaIso);
  }

  public void escribirFechaDeVencimiento(String fechaIso) {
    page.locator("#fechaDeVencimiento").fill(fechaIso);
  }

  public void darClickEnGuardar() {
    page.locator("button[type='submit']").click();
  }
}
