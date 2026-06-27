package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.List;
import java.util.Set;
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
    Set<Categoria> categorias = repositorioCategoria.obtenerTodasLasCategoriasActivas();
    ValidacionHelper.queElSetNoSeaNull(categorias, "categorias para el menú");
    return categorias.stream().map(CategoriaDto::new).collect(Collectors.toList());
  }

  @Override
  public CategoriaDto obtenerCategoriaPorId(Long id) {
    Categoria categoria = repositorioCategoria.buscarPorId(id);
    ValidacionHelper.queNoSeaNull(categoria, "categoria");
    return new CategoriaDto(categoria);
  }
}
