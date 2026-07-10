package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.ServicioCategoria;
import com.tallerwebi.dominio.utils.CarritoPedido;
import com.tallerwebi.dominio.utils.ItemCarrito;
import com.tallerwebi.presentacion.controller.ControladorPedidoCajero;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import java.math.BigDecimal;
import java.util.Arrays;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorPedidoCajeroTest {

  private ServicioCategoria servicioCategoria;
  private ControladorPedidoCajero controlador;
  private HttpSession session;

  @BeforeEach
  public void setUp() {
    servicioCategoria = mock(ServicioCategoria.class);
    controlador = new ControladorPedidoCajero(servicioCategoria);
    session = mock(HttpSession.class);
  }

  @Test
  public void queSePuedaMostrarCaja() {
    CategoriaDto cat1 = new CategoriaDto();
    when(servicioCategoria.obtenerLasCategoriasParaElMenu()).thenReturn(Arrays.asList(cat1));

    ModelAndView mav = controlador.mostrarCaja();

    assertThat(mav.getViewName(), equalTo("caja/caja"));
    assertThat(mav.getModel().get("categorias"), notNullValue());
    verify(servicioCategoria, times(1)).obtenerLasCategoriasParaElMenu();
  }

  @Test
  public void queRedirijaACajaSiElCarritoEsNulo() {
    when(session.getAttribute("carrito")).thenReturn(null);

    ModelAndView mav = controlador.mostrarCobro(session);

    assertThat(mav.getViewName(), equalTo("redirect:/cajero"));
  }

  @Test
  public void queRedirijaACajaSiElCarritoEstaVacio() {
    CarritoPedido carrito = new CarritoPedido();
    when(session.getAttribute("carrito")).thenReturn(carrito);

    ModelAndView mav = controlador.mostrarCobro(session);

    assertThat(mav.getViewName(), equalTo("redirect:/cajero"));
  }

  @Test
  public void queMuestreCobroSiElCarritoTieneItems() {
    CarritoPedido carrito = new CarritoPedido();
    ProductoFinal p1 = new ProductoFinal();
    p1.setNombre("Carne");
    p1.setPrecio(BigDecimal.valueOf(100));
    carrito.agregarItem(new ItemCarrito(p1));

    when(session.getAttribute("carrito")).thenReturn(carrito);

    ModelAndView mav = controlador.mostrarCobro(session);

    assertThat(mav.getViewName(), equalTo("caja/cobro"));
    assertThat(mav.getModel().get("carrito"), notNullValue());
  }
}
