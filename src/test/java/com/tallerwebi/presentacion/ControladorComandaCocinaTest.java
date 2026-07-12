package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import com.tallerwebi.presentacion.controller.ControladorComandaCocina;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.servlet.ModelAndView;

public class ControladorComandaCocinaTest {

  private ServicioCategoria servicioCategoria;
  private ServicioComanda servicioComanda;
  private ControladorComandaCocina controlador;

  @BeforeEach
  public void setUp() {
    servicioCategoria = mock(ServicioCategoria.class);
    servicioComanda = mock(ServicioComanda.class);
    controlador = new ControladorComandaCocina(servicioCategoria, servicioComanda);
  }

  @Test
  public void queUnUsuarioComunPuedaVerSusComandasAsignadas() {
    Authentication auth = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);

    User user = mock(User.class);
    when(user.getUsername()).thenReturn("test@test.com");
    when(auth.getPrincipal()).thenReturn(user);

    Collection authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    doReturn(authorities).when(auth).getAuthorities();

    CategoriaDto cat1 = new CategoriaDto();
    cat1.setId(1L);
    when(servicioCategoria.obtenerCategoriasPorUsuario("test@test.com"))
      .thenReturn(Arrays.asList(cat1));
    when(servicioComanda.contarPendientesPorCategoria(1L)).thenReturn(5);

    ModelAndView mav = controlador.mostrarBotonera();

    assertThat(mav.getViewName(), equalTo("comandas/comandas-categorias"));
    assertThat(mav.getModel().get("categorias"), notNullValue());
    verify(servicioCategoria, times(1)).obtenerCategoriasPorUsuario("test@test.com");
  }

  @Test
  public void queUnAdminPuedaVerTodasLasComandas() {
    Authentication auth = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);

    User user = mock(User.class);
    when(user.getUsername()).thenReturn("admin@test.com");
    when(auth.getPrincipal()).thenReturn(user);

    Collection authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    doReturn(authorities).when(auth).getAuthorities();

    CategoriaDto cat1 = new CategoriaDto();
    cat1.setId(1L);
    when(servicioCategoria.obtenerLasCategoriasParaElMenu()).thenReturn(Arrays.asList(cat1));
    when(servicioComanda.contarPendientesPorCategoria(1L)).thenReturn(10);

    ModelAndView mav = controlador.mostrarBotonera();

    assertThat(mav.getViewName(), equalTo("comandas/comandas-categorias"));
    assertThat(mav.getModel().get("categorias"), notNullValue());
    verify(servicioCategoria, times(1)).obtenerLasCategoriasParaElMenu();
  }

  @Test
  public void queSePuedaMostrarComandasDeCategoria() {
    CategoriaDto cat1 = new CategoriaDto();
    cat1.setId(1L);
    when(servicioCategoria.obtenerCategoriaPorId(1L)).thenReturn(cat1);

    ModelAndView mav = controlador.mostrarComandasDeCategoria(1L);

    assertThat(mav.getViewName(), equalTo("comandas/comandas"));
    assertThat(mav.getModel().get("categoria"), equalTo(cat1));
  }
}
