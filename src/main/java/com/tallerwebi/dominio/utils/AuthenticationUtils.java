package com.tallerwebi.dominio.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class AuthenticationUtils {

  public static String obtenerEmailDeAutenticacion(Authentication authentication) {
    Object principal = authentication.getPrincipal();
    if (principal instanceof OAuth2User) {
      // login con Google
      return ((OAuth2User) principal).getAttribute("email");
    }
    // login normal
    return ((User) principal).getUsername();
  }
}
