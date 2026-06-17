package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioInactivo;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import java.util.Collections;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

  private final RepositorioUsuario repositorioUsuario;
  private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

  public UserDetailsServiceImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Usuario usuario = repositorioUsuario.buscar(email);
    if (logger.isDebugEnabled()) {
      logger.debug("Buscando usuario con email: {}", email);
    }
    if (usuario == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("Usuario no encontrado: {}", email);
      }
      throw new UsernameNotFoundException("Usuario no encontrado: " + email);
    }
    if (!usuario.getActivo()) {
      throw new UsuarioInactivo("El usuario está inactivo");
    }
    verificarCategorias(usuario);
    return new User(
      usuario.getEmail(),
      usuario.getPassword(),
      Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
    );
  }

  private void verificarCategorias(Usuario usuario) {
    boolean noEsAdmin = !"ADMIN".equalsIgnoreCase(usuario.getRol());
    boolean sinCategorias = usuario.getCategorias() == null || usuario.getCategorias().isEmpty();
    if (noEsAdmin && sinCategorias) {
      throw new com.tallerwebi.dominio.excepcion.UsuarioSinCategorias(
        "Todavía no te asignaron una categoría"
      );
    }
  }
}
