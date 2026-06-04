package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.entity.Usuario;
import java.util.List;

public interface RepositorioUsuario {
  Usuario buscarUsuario(String email, String password);
  void guardar(Usuario usuario);
  Usuario buscar(String email);
  Usuario buscarPorTokenValidacion(String tokenValidacion);
  void modificar(Usuario usuario);
  List<Usuario> listarTodos();
  Usuario obtenerPorId(Long id);
}
