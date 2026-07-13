package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.interfaces.ServicioLote;
import com.tallerwebi.dominio.interfaces.ServicioProducto;
import com.tallerwebi.presentacion.controller.ControladorLote;
import com.tallerwebi.presentacion.dto.StockProductoDTO;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLoteTest {

  private ControladorLote controladorLote;
  private ServicioLote servicioLoteMock;
  private ServicioProducto servicioProductoMock;

  @BeforeEach
  public void init() {
    servicioLoteMock = mock(ServicioLote.class);
    servicioProductoMock = mock(ServicioProducto.class);
    controladorLote = new ControladorLote(servicioLoteMock, servicioProductoMock);
  }

  @Test
  public void mostrarLotesDelAlmacenDeberiaRetornarVistaArticulosConLotesYStock() {
    List<Lote> lotes = new ArrayList<>();
    List<StockProductoDTO> stockLotes = new ArrayList<>();
    when(servicioLoteMock.obtenerTodosLosLotes()).thenReturn(lotes);
    when(servicioLoteMock.obtenerStockAgrupado()).thenReturn(stockLotes);

    ModelAndView mav = controladorLote.mostrarLotesDelAlmacen();

    assertEquals("articulos", mav.getViewName());
    assertEquals(lotes, mav.getModel().get("articulos"));
    assertEquals(stockLotes, mav.getModel().get("stockArticulos"));
  }

  @Test
  public void mostrarStockDeberiaRetornarVistaStockConStockArticulos() {
    List<StockProductoDTO> stockLotes = new ArrayList<>();
    when(servicioLoteMock.obtenerStockAgrupado()).thenReturn(stockLotes);

    ModelAndView mav = controladorLote.mostrarStock();

    assertEquals("stock", mav.getViewName());
    assertEquals(stockLotes, mav.getModel().get("stockArticulos"));
  }

  @Test
  public void mostrarFormularioNuevoLoteDeberiaRetornarVistaNuevoArticuloConLoteVacioYProductos() {
    List<Producto> productos = new ArrayList<>();
    when(servicioProductoMock.obtenerTodosLosProductos()).thenReturn(productos);

    ModelAndView mav = controladorLote.mostrarFormularioNuevoLote();

    assertEquals("nuevo-articulo", mav.getViewName());
    assertTrue(mav.getModel().get("articulo") instanceof Lote);
    assertEquals(productos, mav.getModel().get("productos"));
  }

  @Test
  public void registrarLoteExitosoDeberiaRedirigirAlAdmin() {
    Lote lote = new Lote();
    Producto producto = new Producto();
    when(servicioProductoMock.obtenerProductoPorId(1L)).thenReturn(producto);

    ModelAndView mav = controladorLote.registrarLote(
      lote,
      1L,
      "2026-07-13T10:00:00",
      "2026-07-20T10:00:00"
    );

    verify(servicioLoteMock, times(1)).registrarLote(lote);
    assertEquals("redirect:/admin", mav.getViewName());
    assertEquals(producto, lote.getProducto());
  }

  @Test
  public void registrarLoteConProductoInexistenteDeberiaRetornarFormularioConError() {
    Lote lote = new Lote();
    when(servicioProductoMock.obtenerProductoPorId(1L)).thenReturn(null);

    ModelAndView mav = controladorLote.registrarLote(
      lote,
      1L,
      "2026-07-13T10:00:00",
      "2026-07-20T10:00:00"
    );

    assertEquals("nuevo-articulo", mav.getViewName());
    assertEquals(lote, mav.getModel().get("articulo"));
    assertTrue(
      mav.getModel().get("error").toString().contains("El producto seleccionado no existe")
    );
  }

  @Test
  public void registrarLoteConFechaInvalidaDeberiaRetornarFormularioConError() {
    Lote lote = new Lote();
    Producto producto = new Producto();
    when(servicioProductoMock.obtenerProductoPorId(1L)).thenReturn(producto);

    ModelAndView mav = controladorLote.registrarLote(
      lote,
      1L,
      "fecha-invalida",
      "2026-07-20T10:00:00"
    );

    assertEquals("nuevo-articulo", mav.getViewName());
    assertEquals(lote, mav.getModel().get("articulo"));
    assertTrue(mav.getModel().get("error").toString().contains("Error al registrar el lote"));
  }
}
