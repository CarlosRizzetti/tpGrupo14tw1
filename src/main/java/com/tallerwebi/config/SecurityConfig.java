package com.tallerwebi.config;

import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioOAuth2;
import com.tallerwebi.dominio.interfaces.ServicioOAuth2;
import com.tallerwebi.dominio.interfaces.ServicioOAuth2;
import com.tallerwebi.dominio.services.ServicioRecaptcha;
import com.tallerwebi.dominio.services.ServicioRecaptcha;
import com.tallerwebi.dominio.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@PropertySource(value = "classpath:application.properties")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final RepositorioUsuario repositorioUsuario;
  private final ServicioOAuth2 servicioOAuth2;
  private final ServicioRecaptcha servicioRecaptcha;

  @Autowired
  public SecurityConfig(
    RepositorioUsuario repositorioUsuario,
    ServicioOAuth2 servicioOAuth2,
    ServicioRecaptcha servicioRecaptcha
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.servicioOAuth2 = servicioOAuth2;
    this.servicioRecaptcha = servicioRecaptcha;
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Override
  public UserDetailsService userDetailsService() {
    return new UserDetailsServiceImpl(repositorioUsuario);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .addFilterBefore(
        new FiltroRecaptcha(servicioRecaptcha),
        UsernamePasswordAuthenticationFilter.class
      )
      .csrf()
      .disable()
      .exceptionHandling()
      .accessDeniedPage("/home")
      .and()
      .authorizeRequests()
      .antMatchers("/admin", "/admin/**")
      .hasRole("ADMIN")
      .antMatchers(
        "/login",
        "/registrarme",
        "/validar-login",
        "/",
        "/nuevo-usuario",
        "/validacion-identidad",
        "/resources/**"
      )
      .permitAll()
      .anyRequest()
      .authenticated()
      .and()
      .formLogin()
      .loginPage("/login")
      .defaultSuccessUrl("/home", true)
      .permitAll()
      .and()
      .oauth2Login()
      .loginPage("/login")
      .userInfoEndpoint()
      .userService((OAuth2UserService<OAuth2UserRequest, OAuth2User>) userDetailsService())
      .and()
      .successHandler(oAuth2SuccessHandler())
      .and()
      .logout()
      .permitAll();
  }

  @Bean
  public AuthenticationSuccessHandler oAuth2SuccessHandler() {
    return (request, response, authentication) -> {
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
      String email = oAuth2User.getAttribute("email");
      String nombre = oAuth2User.getAttribute("name");
      servicioOAuth2.procesarUsuarioGoogle(email, nombre, response);
    };
  }

  @Bean
  public static ClientRegistrationRepository clientRegistrationRepository(
    @Value("${google.client-id}") String googleClientId,
    @Value("${google.client-secret}") String googleClientSecret
  ) {
    ClientRegistration google = ClientRegistration
      .withRegistrationId("google")
      .clientId(googleClientId)
      .clientSecret(googleClientSecret)
      .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
      .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
      .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
      .scope("email", "profile")
      .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
      .tokenUri("https://www.googleapis.com/oauth2/v4/token")
      .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
      .userNameAttributeName("email")
      .clientName("Google")
      .build();

    return new InMemoryClientRegistrationRepository(google);
  }
}
