package com.tallerwebi.presentacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoriaComandasDTO {

  private CategoriaDto categoria;
  private int comandasPendientes;
}
