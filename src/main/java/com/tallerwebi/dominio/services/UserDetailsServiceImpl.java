package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.EstadoUsuario;
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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserDetailsServiceImpl
  implements UserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final RepositorioUsuario repositorioUsuario;
  private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

  public UserDetailsServiceImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Usuario usuario = repositorioUsuario.buscar(email);

    if (usuario == null) {
      throw new UsernameNotFoundException("Usuario no encontrado: " + email);
    }
    if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
      throw new UsuarioInactivo("El usuario no está activo");
    }
    verificarCategorias(usuario);
    return new User(
      usuario.getEmail(),
      usuario.getPassword(),
      Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
    );
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

    String email = oAuth2User.getAttribute("email");

    Usuario usuario = repositorioUsuario.buscar(email);

    if (usuario == null) {
      String nombre = oAuth2User.getAttribute("name");
      usuario = new Usuario();
      usuario.setEmail(email);
      usuario.setNombre(nombre);
      usuario.setEstado(EstadoUsuario.PENDIENTE);
      usuario.setRol("USER");
      repositorioUsuario.guardar(usuario);
    }

    return new DefaultOAuth2User(
      Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())),
      oAuth2User.getAttributes(),
      "email"
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
