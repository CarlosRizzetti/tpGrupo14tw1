package com.tallerwebi.presentacion.dto;

public class ArticuloJsonDto {

  private Long id;
  private String nombre;
  private String marca;
  private String proveedor;

  public ArticuloJsonDto(Long id, String nombre, String marca, String proveedor) {
    this.id = id;
    this.nombre = nombre;
    this.marca = marca;
    this.proveedor = proveedor;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getMarca() {
    return marca;
  }

  public void setMarca(String marca) {
    this.marca = marca;
  }

  public String getProveedor() {
    return proveedor;
  }

  public void setProveedor(String proveedor) {
    this.proveedor = proveedor;
  }
}
