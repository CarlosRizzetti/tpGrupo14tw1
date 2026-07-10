package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.ArrayList;
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
  private final RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioCategoriaImpl(
    RepositorioCategoria repositorioCategoria,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioCategoria = repositorioCategoria;
    this.repositorioUsuario = repositorioUsuario;
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

  @Override
  public List<CategoriaDto> obtenerCategoriasPorUsuario(String email) {
    Usuario usuario = repositorioUsuario.buscar(email);
    if (usuario != null && usuario.getCategorias() != null) {
      return usuario.getCategorias().stream().map(CategoriaDto::new).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  @Override
  public List<Usuario> obtenerUsuariosPorCategoria(Long categoriaId) {
    List<Usuario> usuarios = repositorioUsuario.listarLosUsuariosDeLasCategorias(categoriaId);
    if (usuarios != null) {
      return usuarios;
    }
    return new ArrayList<>();
  }
}
