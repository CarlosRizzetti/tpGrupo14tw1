package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioCategoria")
@Transactional
public class ServicioCategoriaImpl implements ServicioCategoria {

  public RepositorioCategoria repositorioCategoria;

  @Autowired
  public ServicioCategoriaImpl(RepositorioCategoria repositorioCategoria) {
    this.repositorioCategoria = repositorioCategoria;
  }

  @Override
  public List<CategoriaDto> obtenerLasCategoriasParaElMenu() {
    List<Categoria> categorias = repositorioCategoria.obtenerTodasLasCategoriasActivas();
    return categorias
      .stream()
      .map(categoria -> new CategoriaDto(categoria))
      .collect(Collectors.toList());
  }
}
