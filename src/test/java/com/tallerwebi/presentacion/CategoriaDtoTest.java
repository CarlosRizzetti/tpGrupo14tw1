package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.Categoria;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CategoriaDtoTest {

  Categoria categoria;
  CategoriaDto categoriaDTO;

  @BeforeEach
  public void init() {
    this.categoria = new Categoria("mccafe.png", true, "mccafe");
    this.categoriaDTO = new CategoriaDto(categoria);
  }

  @Test
  public void queSeDefinaElTemaSegunElNombre() {
    assertThat(categoriaDTO.getTema(), equalToIgnoringCase("tema-mccafe"));
    Categoria categoriaServicio = new Categoria("servicio.png", true, "servicio");
    CategoriaDto categoriaServicioDTO = new CategoriaDto(categoriaServicio);
    assertThat(categoriaServicioDTO.getTema(), equalToIgnoringCase("tema-servicio"));
    Categoria categoriaIsla = new Categoria("isla.png", true, "isla");
    CategoriaDto categoriaIslaDTO = new CategoriaDto(categoriaIsla);
    assertThat(categoriaIslaDTO.getTema(), equalToIgnoringCase("tema-isla"));
    Categoria categoriaDefault = new Categoria("default.png", true, "default");
    CategoriaDto categoriaDefaultDTO = new CategoriaDto(categoriaDefault);
    assertThat(categoriaDefaultDTO.getTema(), equalToIgnoringCase("tema-cocina"));
  }

  @Test
  public void verificarQueLosDatosSePasenBienAlDTO() {
    assertThat(categoriaDTO.getNombre(), equalToIgnoringCase("mccafe"));
    assertThat(categoriaDTO.getIcono(), equalToIgnoringCase("mccafe.png"));
    assertEquals(categoriaDTO.getId(), categoria.getId());
  }
}
