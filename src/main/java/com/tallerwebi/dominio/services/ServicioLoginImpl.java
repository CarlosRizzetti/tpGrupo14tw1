package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.PasswordInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInactivo;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import com.tallerwebi.dominio.utils.ValidadorPassword;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioLoginImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Usuario consultarUsuario(String email, String password) throws UsuarioInactivo {
    Usuario usuario = repositorioUsuario.buscarUsuario(email, password);
    if (usuario != null && !usuario.getActivo()) {
      throw new UsuarioInactivo();
    }
    return usuario;
  }

  @Override
  public void registrar(Usuario usuario) throws UsuarioExistente, PasswordInvalida {
    if (!ValidadorPassword.esValida(usuario.getPassword())) {
      throw new PasswordInvalida("La contraseña no cumple con los requisitos de seguridad.");
    }

    Usuario usuarioEncontrado = repositorioUsuario.buscar(usuario.getEmail());
    if (usuarioEncontrado != null) {
      throw new UsuarioExistente();
    }
    usuario.setRol("USER");
    usuario.setActivo(true);
    repositorioUsuario.guardar(usuario);
  }
}
