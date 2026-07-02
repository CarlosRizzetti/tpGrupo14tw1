package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioReceta;
import com.tallerwebi.presentacion.controller.ControladorReceta;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRecetaTest {

  private ControladorReceta controladorReceta;
  private ServicioReceta servicioRecetaMock;
  private ServicioProducto servicioProductoMock;
  private ServicioArticulo servicioArticuloMock;

  @BeforeEach
  public void init() {
    servicioRecetaMock = mock(ServicioReceta.class);
    servicioProductoMock = mock(ServicioProducto.class);
    servicioArticuloMock = mock(ServicioArticulo.class);
    controladorReceta =
      new ControladorReceta(servicioRecetaMock, servicioProductoMock, servicioArticuloMock);
  }

  @Test
  public void listarRecetasDeberiaRetornarVistaLista() {
    ModelAndView mav = controladorReceta.listarRecetas();
    assertThat(mav.getViewName(), equalToIgnoringCase("recetas/lista"));
  }

  @Test
  public void formularioNuevaRecetaDeberiaRetornarVistaFormulario() {
    ModelAndView mav = controladorReceta.formularioNuevaReceta();
    assertThat(mav.getViewName(), equalToIgnoringCase("recetas/formulario"));
  }

  @Test
  public void guardarRecetaDeberiaRedirigirAAdminRecetas() {
    Producto productoMock = new Producto();
    when(servicioProductoMock.obtenerProductoPorId(1L)).thenReturn(productoMock);

    ModelAndView mav = controladorReceta.guardarReceta(1L, Arrays.asList(1L), Arrays.asList(2.0));

    verify(servicioRecetaMock, times(1))
      .guardarReceta(productoMock, Arrays.asList(1L), Arrays.asList(2.0));
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/recetas"));
  }

  @Test
  public void guardarRecetaConProductoInexistenteNoDeberiaLlamarAlServicio() {
    when(servicioProductoMock.obtenerProductoPorId(1L)).thenReturn(null);

    ModelAndView mav = controladorReceta.guardarReceta(1L, Arrays.asList(1L), Arrays.asList(2.0));

    verify(servicioRecetaMock, never()).guardarReceta(any(), any(), any());
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/admin/recetas"));
  }
}
