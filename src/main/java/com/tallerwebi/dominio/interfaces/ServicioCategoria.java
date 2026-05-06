package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.presentacion.DatosCategoria;
import java.util.List;

public interface ServicioCategoria {
  List<DatosCategoria> obtenerLasCategoriasParaElMenu();
}
