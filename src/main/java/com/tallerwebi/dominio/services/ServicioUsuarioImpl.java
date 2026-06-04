package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import com.tallerwebi.dominio.utils.ValidadorPassword;
import com.tallerwebi.presentacion.dto.UsuarioDto;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioUsuario")
@Transactional
public class ServicioUsuarioImpl implements ServicioUsuario {

  private final RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioUsuarioImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public List<Usuario> listarUsuarios() {
    return repositorioUsuario.listarTodos();
  }

  @Override
  public void crearUsuario(UsuarioDto dto) throws UsuarioExistente, PasswordInvalida {
    if (!ValidadorPassword.esValida(dto.getPassword())) {
      throw new PasswordInvalida("La contraseña no cumple con los requisitos de seguridad.");
    }
    if (repositorioUsuario.buscar(dto.getEmail()) != null) {
      throw new UsuarioExistente();
    }
    Usuario usuario = new Usuario();
    usuario.setEmail(dto.getEmail());
    usuario.setPassword(dto.getPassword());
    usuario.setRol(dto.getRol() != null ? dto.getRol() : "USER");
    usuario.setActivo(true);
    repositorioUsuario.guardar(usuario);
  }

  @Override
  public Usuario obtenerUsuarioPorId(Long id) {
    return repositorioUsuario.obtenerPorId(id);
  }

  @Override
  public void editarUsuario(Long id, UsuarioDto dto) throws PasswordInvalida {
    Usuario usuario = repositorioUsuario.obtenerPorId(id);
    if (usuario == null) {
      throw new IllegalArgumentException("Usuario no encontrado");
    }
    usuario.setEmail(dto.getEmail());
    if (dto.getRol() != null && !dto.getRol().isEmpty()) {
      usuario.setRol(dto.getRol());
    }
    if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
      if (!ValidadorPassword.esValida(dto.getPassword())) {
        throw new PasswordInvalida("La contraseña no cumple con los requisitos de seguridad.");
      }
      usuario.setPassword(dto.getPassword());
    }
    repositorioUsuario.modificar(usuario);
  }

  @Override
  public void darDeBaja(Long id) {
    Usuario usuario = repositorioUsuario.obtenerPorId(id);
    if (usuario == null) {
      throw new IllegalArgumentException("Usuario no encontrado");
    }
    usuario.setActivo(false);
    repositorioUsuario.modificar(usuario);
  }

  @Override
  public void reactivar(Long id) {
    Usuario usuario = repositorioUsuario.obtenerPorId(id);
    if (usuario == null) {
      throw new IllegalArgumentException("Usuario no encontrado");
    }
    usuario.setActivo(true);
    repositorioUsuario.modificar(usuario);
  }
}
