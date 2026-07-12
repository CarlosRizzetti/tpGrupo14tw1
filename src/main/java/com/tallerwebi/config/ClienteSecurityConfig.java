package com.tallerwebi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(1)
public class ClienteSecurityConfig extends WebSecurityConfigurerAdapter {

  private final ClienteAuthenticationProvider clienteAuthenticationProvider;

  @Autowired
  public ClienteSecurityConfig(ClienteAuthenticationProvider clienteAuthenticationProvider) {
    this.clienteAuthenticationProvider = clienteAuthenticationProvider;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(clienteAuthenticationProvider);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .antMatcher("/portal/clientes/**")
      .csrf()
      .disable()
      .authorizeRequests()
      .antMatchers(
        "/portal/clientes",
        "/portal/clientes/procesar",
        "/portal/clientes/registro",
        "/portal/clientes/registro-procesar",
        "/portal/clientes/google-login"
      )
      .permitAll()
      .anyRequest()
      .hasRole("CLIENTE")
      .and()
      .formLogin()
      .loginPage("/portal/clientes")
      .loginProcessingUrl("/portal/clientes/procesar")
      .usernameParameter("identificador")
      .passwordParameter("password")
      .defaultSuccessUrl("/portal/clientes/mis-pedidos", true)
      .failureUrl("/portal/clientes?error=true")
      .permitAll()
      .and()
      .logout()
      .logoutUrl("/portal/clientes/logout")
      .logoutSuccessUrl("/portal/clientes?logout=true")
      .permitAll();
  }
}
