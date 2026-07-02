package com.tallerwebi.dominio.interfaces;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public interface ServicioOAuth2 {
  public void procesarUsuarioGoogle(String email, String nombre, HttpServletResponse response)
    throws IOException;
}
