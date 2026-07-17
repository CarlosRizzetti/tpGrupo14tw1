package com.tallerwebi.config;

import java.io.IOException;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

public class FiltroRestriccionCajero extends OncePerRequestFilter {

  private static final String PREFIJO_CAJERO = "/cajero/**";
  private static final String ROL_CAJERO = "ROLE_CAJERO";

  // Mismas rutas que el permitAll() de configure(HttpSecurity), más /cajero.
  private static final List<String> PATRONES_PUBLICOS = List.of(
    PREFIJO_CAJERO,
    "/login",
    "/login-oauth",
    "/registrarme",
    "/validar-login",
    "/",
    "/nuevo-usuario",
    "/validacion-identidad",
    "/portal/**",
    "/cliente/**",
    "/resources/**",
    "/webjars/**",
    "/logout",
    "/api/cajero/**"
  );

  private final AntPathMatcher matcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();

    if (esCajero(autenticacion) && !esRutaPermitidaParaCajero(request)) {
      response.sendRedirect(request.getContextPath() + "/cajero");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean esCajero(Authentication autenticacion) {
    return (
      autenticacion != null &&
      autenticacion.isAuthenticated() &&
      autenticacion
        .getAuthorities()
        .stream()
        .anyMatch(authority -> authority.getAuthority().equals(ROL_CAJERO))
    );
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private boolean esRutaPermitidaParaCajero(HttpServletRequest request) {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    return PATRONES_PUBLICOS.stream().anyMatch(patron -> matcher.match(patron, path));
  }
}
