package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaDetalleLote extends VistaWeb {

  public VistaDetalleLote(Page page) {
    super(page);
  }

  public String obtenerNombreProducto() {
    return this.obtenerTextoDelElemento("#lote-nombre-producto");
  }

  public String obtenerEstado() {
    return this.obtenerTextoDelElemento("#lote-estado");
  }

  public String obtenerNumeroDeLote() {
    return this.obtenerTextoDelElemento("#lote-numero");
  }

  public String obtenerProveedor() {
    return this.obtenerTextoDelElemento("#lote-proveedor");
  }

  public String obtenerMarca() {
    return this.obtenerTextoDelElemento("#lote-marca");
  }
}
