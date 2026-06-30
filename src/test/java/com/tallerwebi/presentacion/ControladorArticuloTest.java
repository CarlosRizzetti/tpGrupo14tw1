package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Articulos;
import com.tallerwebi.dominio.interfaces.ServicioArticulo;
import com.tallerwebi.presentacion.controller.ControladorArticulo;
import com.tallerwebi.presentacion.dto.StockArticuloDto;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorArticuloTest {

  private ControladorArticulo controladorArticulo;
  private ServicioArticulo servicioArticuloMock;

  @BeforeEach
  public void init() {
    servicioArticuloMock = mock(ServicioArticulo.class);
    controladorArticulo = new ControladorArticulo(servicioArticuloMock);
  }

  @Test
  public void mostrarArticulosDelAlmacenDeberiaRetornarVistaArticulos() {
    // preparacion
    List<Articulos> articulos = Collections.singletonList(new Articulos());
    List<StockArticuloDto> stockArticulos = Collections.singletonList(new StockArticuloDto());
    when(servicioArticuloMock.obtenerTodosLosArticulos()).thenReturn(articulos);
    when(servicioArticuloMock.obtenerStockAgrupado()).thenReturn(stockArticulos);

    // ejecucion
    ModelAndView mav = controladorArticulo.mostrarArticulosDelAlmacen();

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("articulos"));
    assertThat(mav.getModel().get("articulos"), equalTo(articulos));
    assertThat(mav.getModel().get("stockArticulos"), equalTo(stockArticulos));
  }

  @Test
  public void mostrarStockDeberiaRetornarVistaStock() {
    // preparacion
    List<StockArticuloDto> stockArticulos = Collections.singletonList(new StockArticuloDto());
    when(servicioArticuloMock.obtenerStockAgrupado()).thenReturn(stockArticulos);

    // ejecucion
    ModelAndView mav = controladorArticulo.mostrarStock();

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("stock"));
    assertThat(mav.getModel().get("stockArticulos"), equalTo(stockArticulos));
  }

  @Test
  public void mostrarFormularioNuevoArticuloDeberiaRetornarVistaNuevoArticulo() {
    // ejecucion
    ModelAndView mav = controladorArticulo.mostrarFormularioNuevoArticulo();

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("nuevo-articulo"));
    assertThat(mav.getModel().get("articulo"), notNullValue());
  }

  @Test
  public void registrarArticuloConFechasExitosoDeberiaRedirigirAArticulos() throws Exception {
    // preparacion
    Articulos articulo = new Articulos();

    // ejecucion
    ModelAndView mav = controladorArticulo.registrarArticulo(
      articulo,
      "2023-01-01T10:00",
      "2024-01-01T10:00"
    );

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/almacen/articulos"));
    verify(servicioArticuloMock, times(1)).registrarArticulo(articulo);
  }

  @Test
  public void registrarArticuloSinFechasExitosoDeberiaRedirigirAArticulos() throws Exception {
    // preparacion
    Articulos articulo = new Articulos();

    // ejecucion
    ModelAndView mav = controladorArticulo.registrarArticulo(articulo, null, "");

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/almacen/articulos"));
    verify(servicioArticuloMock, times(1)).registrarArticulo(articulo);
  }

  @Test
  public void registrarArticuloConErrorDeberiaRetornarVistaNuevoArticulo() throws Exception {
    // preparacion
    Articulos articulo = new Articulos();
    doThrow(new RuntimeException("Error")).when(servicioArticuloMock).registrarArticulo(articulo);

    // ejecucion
    ModelAndView mav = controladorArticulo.registrarArticulo(articulo, "", "");

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("nuevo-articulo"));
    assertThat(mav.getModel().get("error"), notNullValue());
  }

  @Test
  public void registrarArticuloConFechasInvalidasDeberiaRetornarVistaConError() throws Exception {
    // preparacion
    Articulos articulo = new Articulos();

    // ejecucion
    ModelAndView mav = controladorArticulo.registrarArticulo(
      articulo,
      "fecha-invalida",
      "otra-invalida"
    );

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("nuevo-articulo"));
    assertThat(
      mav.getModel().get("error").toString(),
      containsString("Error al registrar el articulo")
    );
  }

  @Test
  public void registrarArticuloConFechasNulasDeberiaRedirigirAArticulos() throws Exception {
    // preparacion
    Articulos articulo = new Articulos();

    // ejecucion
    ModelAndView mav = controladorArticulo.registrarArticulo(articulo, null, null);

    // validacion
    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/almacen/articulos"));
    verify(servicioArticuloMock, times(1)).registrarArticulo(articulo);
  }
}
