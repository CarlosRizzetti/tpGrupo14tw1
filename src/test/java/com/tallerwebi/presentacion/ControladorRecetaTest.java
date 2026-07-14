package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.dominio.interfaces.ServicioProductoFinal;
import com.tallerwebi.presentacion.controller.ControladorReceta;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRecetaTest {

  private ServicioProductoFinal servicioProductoFinal;
  private ServicioCategoria servicioCategoria;
  private ServicioProducto servicioProducto;
  private ControladorReceta controladorReceta;

  @BeforeEach
  public void init() {
    servicioProductoFinal = mock(ServicioProductoFinal.class);
    servicioCategoria = mock(ServicioCategoria.class);
    servicioProducto = mock(ServicioProducto.class);
    controladorReceta =
      new ControladorReceta(servicioProductoFinal, servicioCategoria, servicioProducto);
  }

  @Test
  public void mostrarRecetasDeberiaDevolverVistaRecetas() {
    ModelAndView modelAndView = controladorReceta.mostrarRecetas();
    assertEquals("funcionalidadesAdmin/recetas", modelAndView.getViewName());
    verify(servicioCategoria, times(1)).obtenerLasCategoriasParaElMenu();
    verify(servicioProducto, times(1)).obtenerTodosLosProductos();
    verify(servicioProductoFinal, times(1)).listarTodos();
  }

  @Test
  public void guardarRecetaDeberiaRedirigirConExito() {
    ProductoFinal productoFinal = new ProductoFinal();
    Long categoriaId = 1L;
    List<Long> ingredientesIds = new ArrayList<>();
    List<Integer> cantidades = new ArrayList<>();

    ModelAndView modelAndView = controladorReceta.guardarReceta(
      productoFinal,
      categoriaId,
      ingredientesIds,
      cantidades
    );

    assertEquals("redirect:/admin/recetas?exito", modelAndView.getViewName());
    verify(servicioProductoFinal, times(1))
      .guardarProductoFinal(productoFinal, categoriaId, ingredientesIds, cantidades);
  }
}
