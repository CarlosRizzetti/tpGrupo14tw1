package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;

public interface ServicioCategoria {
  List<CategoriaDto> obtenerLasCategoriasParaElMenu();
}
