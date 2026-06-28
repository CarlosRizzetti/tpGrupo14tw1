package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioAdmin;
import com.tallerwebi.presentacion.controller.ControladorAdmin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorAdminTest {

  private ServicioAdmin servicioAdmin;
  private ControladorAdmin controladorAdmin;

  @BeforeEach
  public void init() {
    servicioAdmin = mock(ServicioAdmin.class);
    controladorAdmin = new ControladorAdmin(servicioAdmin);
  }

  @Test
  public void alAprobarUsuarioDebeRedirigirAAdminUsuariosYEjecutarServicio() {
    Long idUsuario = 1L;
    Long idCategoria = 2L;

    ModelAndView mav = controladorAdmin.aprobarUsuario(idUsuario, idCategoria);

    verify(servicioAdmin, times(1)).aprobarUsuario(idUsuario, idCategoria);
    assertThat(mav.getViewName(), equalTo("redirect:/admin/usuarios"));
  }
}
