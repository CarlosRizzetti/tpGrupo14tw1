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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private RepositorioUsuario repositorioUsuario;
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  public ServicioLoginImpl(
    RepositorioUsuario repositorioUsuario,
    BCryptPasswordEncoder passwordEncoder
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Usuario consultarUsuario(String email, String password)
    throws UsuarioInactivo, PasswordInvalida {
    Usuario usuario = repositorioUsuario.buscarUsuario(email);

    if (usuario == null || !passwordEncoder.matches(password, usuario.getPassword())) {
      throw new PasswordInvalida("Usuario o clave incorrecta");
    }
    if (!usuario.getActivo()) throw new UsuarioInactivo("El usuario está inactivo");

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
    usuario.setActivo(false);
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    repositorioUsuario.guardar(usuario);
  }
}
