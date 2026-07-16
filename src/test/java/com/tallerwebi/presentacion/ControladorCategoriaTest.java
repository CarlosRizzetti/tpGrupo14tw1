package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.presentacion.controller.ControladorCategoria;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.ModelAndView;

public class ControladorCategoriaTest {

  private ServicioCategoria servicioCategoria;
  private ControladorCategoria controladorCategoria;

  @BeforeEach
  public void init() {
    servicioCategoria = mock(ServicioCategoria.class);
    controladorCategoria = new ControladorCategoria(servicioCategoria);
  }

  @Test
  public void siEsAdminDebeTraerTodasLasCategorias() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    List<GrantedAuthority> authorities = Collections.singletonList(
      new SimpleGrantedAuthority("ROLE_ADMIN")
    );
    doReturn(authorities).when(auth).getAuthorities();

    List<CategoriaDto> categorias = new ArrayList<>();
    categorias.add(new CategoriaDto());
    when(servicioCategoria.obtenerLasCategoriasParaElMenu()).thenReturn(categorias);

    ModelAndView mav = controladorCategoria.index(auth);

    verify(servicioCategoria, times(1)).obtenerLasCategoriasParaElMenu();
    assertThat(mav.getViewName(), equalTo("vencimientos-categorias"));
    assertThat(mav.getModel().get("categorias"), equalTo(categorias));
  }

  @Test
  public void siUsuarioAutenticadoDebeTraerTodasLasCategorias() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getName()).thenReturn("test@test.com");

    List<CategoriaDto> categorias = new ArrayList<>();
    categorias.add(new CategoriaDto());
    when(servicioCategoria.obtenerLasCategoriasParaElMenu()).thenReturn(categorias);

    ModelAndView mav = controladorCategoria.index(auth);

    verify(servicioCategoria, times(1)).obtenerLasCategoriasParaElMenu();
    assertThat(mav.getViewName(), equalTo("vencimientos-categorias"));
    assertThat(mav.getModel().get("categorias"), equalTo(categorias));
  }

  @Test
  public void siLaListaDeCategoriasEstaVaciaDebeMostrarMensaje() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getName()).thenReturn("test@test.com");

    when(servicioCategoria.obtenerLasCategoriasParaElMenu()).thenReturn(new ArrayList<>());

    ModelAndView mav = controladorCategoria.index(auth);

    assertTrue(mav.getModel().containsKey("mensajeVacio"));
    assertThat(mav.getModel().get("mensajeVacio"), equalTo("No hay categorías disponibles"));
  }
}
