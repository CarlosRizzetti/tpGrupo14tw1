package com.tallerwebi.config;

import com.tallerwebi.dominio.services.ServicioRecaptcha;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

public class FiltroRecaptcha extends OncePerRequestFilter {

  private final ServicioRecaptcha servicioRecaptcha;

  private final String POST = "POST";
  private final String LOGIN_URL = "/login";

  public FiltroRecaptcha(ServicioRecaptcha servicioRecaptcha) {
    this.servicioRecaptcha = servicioRecaptcha;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    if (POST.equalsIgnoreCase(request.getMethod()) && LOGIN_URL.equals(request.getRequestURI())) {
      String recaptchaResponse = request.getParameter("g-recaptcha-response");

      if (!servicioRecaptcha.verificar(recaptchaResponse)) {
        response.sendRedirect("/login?error=recaptcha");
        return;
      }
    }
    filterChain.doFilter(request, response);
  }
}
