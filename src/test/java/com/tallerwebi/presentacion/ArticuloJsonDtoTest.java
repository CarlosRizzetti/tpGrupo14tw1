package com.tallerwebi.presentacion.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

public class ArticuloJsonDtoTest {

  @Test
  public void queSePuedaCrearYObtenerValores() {
    ArticuloJsonDto dto = new ArticuloJsonDto(1L, "Test Nombre", "Test Marca", "Test Proveedor");

    assertThat(dto.getId(), equalTo(1L));
    assertThat(dto.getNombre(), equalTo("Test Nombre"));
    assertThat(dto.getMarca(), equalTo("Test Marca"));
    assertThat(dto.getProveedor(), equalTo("Test Proveedor"));

    dto.setId(2L);
    dto.setNombre("Nuevo Nombre");
    dto.setMarca("Nueva Marca");
    dto.setProveedor("Nuevo Proveedor");

    assertThat(dto.getId(), equalTo(2L));
    assertThat(dto.getNombre(), equalTo("Nuevo Nombre"));
    assertThat(dto.getMarca(), equalTo("Nueva Marca"));
    assertThat(dto.getProveedor(), equalTo("Nuevo Proveedor"));
  }
}
