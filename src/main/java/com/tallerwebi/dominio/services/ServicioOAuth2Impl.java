package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.EstadoUsuario;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioOAuth2;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioOAuth2Impl implements ServicioOAuth2 {

  private final RepositorioUsuario repositorioUsuario;

  public ServicioOAuth2Impl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public void procesarUsuarioGoogle(String email, String nombre, HttpServletResponse response)
    throws IOException {
    Usuario usuario = repositorioUsuario.buscar(email);

    if (usuario == null) {
      usuario = new Usuario();
      usuario.setEmail(email);
      usuario.setNombre(nombre);
      usuario.setEstado(EstadoUsuario.PENDIENTE);
      usuario.setRol("USER");
      repositorioUsuario.guardar(usuario);
    }

    if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
      response.sendRedirect("/login-oauth?pendiente=true");
      return;
    }

    boolean noEsAdmin = !"ADMIN".equalsIgnoreCase(usuario.getRol());
    boolean sinCategorias = usuario.getCategorias() == null || usuario.getCategorias().isEmpty();
    if (noEsAdmin && sinCategorias) {
      response.sendRedirect("/login-oauth?sinCategorias=true");
    }

    response.sendRedirect("/home");
  }
}
