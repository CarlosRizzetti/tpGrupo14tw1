package com.tallerwebi.config;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ClienteAuthenticationProvider implements AuthenticationProvider {

  private final ServicioCliente servicioCliente;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Autowired
  public ClienteAuthenticationProvider(ServicioCliente servicioCliente) {
    this.servicioCliente = servicioCliente;
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String identificador = authentication.getName();
    String password = (authentication.getCredentials() != null)
      ? authentication.getCredentials().toString()
      : "";

    Cliente cliente = servicioCliente.buscarPorEmail(identificador);
    if (cliente == null) {
      cliente = servicioCliente.buscarPorDocumento(identificador);
    }

    if (cliente != null && cliente.getPassword() != null) {
      boolean passwordValida =
        passwordEncoder.matches(password, cliente.getPassword()) ||
        password.equals(cliente.getPassword());
      if (passwordValida) {
        return new UsernamePasswordAuthenticationToken(
          cliente,
          password,
          Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
      }
    }
    throw new BadCredentialsException("Correo/DNI o contraseña incorrectos");
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
