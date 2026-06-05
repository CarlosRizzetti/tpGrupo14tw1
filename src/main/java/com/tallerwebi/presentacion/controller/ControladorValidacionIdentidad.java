package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.interfaces.ServicioValidacionIdentidad;
import com.tallerwebi.presentacion.dto.ResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorValidacionIdentidad {

  private static final String MENSAJE_SOLICITUD =
    "Si el correo existe, hemos enviado un enlace de validación";
  private static final String VISTA_VALIDACION = "loginYRegistro/validacion-identidad";
  private static final String REDIRECT_URL = "/login";
  private static final int REDIRECT_DELAY_MS = 3000;

  private final ServicioValidacionIdentidad servicioValidacionIdentidad;

  @Autowired
  public ControladorValidacionIdentidad(ServicioValidacionIdentidad servicioValidacionIdentidad) {
    this.servicioValidacionIdentidad = servicioValidacionIdentidad;
  }

  @PostMapping("/validar-identidad")
  public ResponseEntity<ResponseDTO> solicitarValidacion(@RequestParam("email") String email) {
    servicioValidacionIdentidad.solicitarValidacion(email);
    return ResponseEntity.ok(new ResponseDTO(true, MENSAJE_SOLICITUD));
  }

  @GetMapping("/validar-identidad")
  public ResponseEntity<ResponseDTO> validarIdentidad(@RequestParam("token") String token) {
    boolean activado = servicioValidacionIdentidad.validarToken(token);
    ResponseDTO response = new ResponseDTO();
    response.setSuccess(activado);
    response.setMessage(activado ? "Cuenta activada correctamente" : "Token inválido o expirado");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/validacion-identidad")
  public ModelAndView mostrarValidacion(
    @RequestParam(value = "token", required = false) String token
  ) {
    return construirVistaValidacion(token);
  }

  @PostMapping("/validacion-identidad")
  public ModelAndView validarIdentidadManual(@RequestParam("token") String token) {
    return construirVistaValidacion(token);
  }

  private ModelAndView construirVistaValidacion(String token) {
    ModelMap modelo = new ModelMap();
    if (token != null && !token.trim().isEmpty()) {
      boolean activado = servicioValidacionIdentidad.validarToken(token);
      if (activado) {
        modelo.put("success", true);
        modelo.put("message", "Cuenta activada correctamente");
        modelo.put("redirectUrl", REDIRECT_URL);
        modelo.put("redirectDelayMs", REDIRECT_DELAY_MS);
      } else {
        modelo.put("success", false);
        modelo.put("message", "Token inválido o expirado");
      }
    }
    return new ModelAndView(VISTA_VALIDACION, modelo);
  }
}
