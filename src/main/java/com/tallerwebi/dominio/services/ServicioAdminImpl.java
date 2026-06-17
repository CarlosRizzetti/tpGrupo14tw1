package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioAdmin;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioAdminImpl implements ServicioAdmin {

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioCategoria repositorioCategoria;

  @Autowired
  public ServicioAdminImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioCategoria repositorioCategoria
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioCategoria = repositorioCategoria;
  }

  @Override
  public void aprobarUsuario(Long idUsuario, Long idCategoria) {
    Usuario usuario = repositorioUsuario.obtenerPorId(idUsuario);
    Categoria categoria = repositorioCategoria.buscarPorId(idCategoria);
    if (usuario != null && categoria != null) {
      usuario.setActivo(true);
      if (!usuario.getCategorias().contains(categoria)) {
        usuario.getCategorias().add(categoria);
      }
      repositorioUsuario.modificar(usuario);
    }
  }
}
