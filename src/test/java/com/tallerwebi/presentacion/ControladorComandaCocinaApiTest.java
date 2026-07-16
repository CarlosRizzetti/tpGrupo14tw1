package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import com.tallerwebi.presentacion.controller.ControladorComandaCocinaApi;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControladorComandaCocinaApiTest {

  private ServicioComanda servicioComanda;
  private ControladorComandaCocinaApi controlador;

  @BeforeEach
  void init() {
    servicioComanda = mock(ServicioComanda.class);
    controlador = new ControladorComandaCocinaApi(servicioComanda);
  }

  private Producto crearProducto(Long id, String nombre) {
    Producto producto = new Producto();
    producto.setId(id);
    producto.setNombre(nombre);
    return producto;
  }

  // ========================================================
  // GET /api/cocina/comandas
  // ========================================================

  @Test
  @DisplayName(
    "HP-01 | listarPorCategoria | Devuelve la lista que provee el servicio para esa categoría"
  )
  void listarPorCategoriaDeberiaDevolverLoQueDevuelveElServicio() {
    ComandaCocinaDTO dto = new ComandaCocinaDTO();
    dto.setIdSector(10L);
    when(servicioComanda.listarPendientesPorCategoria(5L)).thenReturn(List.of(dto));

    List<ComandaCocinaDTO> resultado = controlador.listarPorCategoria(5L);

    assertEquals(1, resultado.size());
    assertEquals(10L, resultado.get(0).getIdSector());
    verify(servicioComanda).listarPendientesPorCategoria(5L);
  }

  @Test
  @DisplayName(
    "EDGE-01 | listarPorCategoria | Devuelve una lista vacía si el servicio no tiene pendientes"
  )
  void listarPorCategoriaDeberiaDevolverListaVaciaSiNoHayPendientes() {
    when(servicioComanda.listarPendientesPorCategoria(5L)).thenReturn(List.of());

    List<ComandaCocinaDTO> resultado = controlador.listarPorCategoria(5L);

    assertTrue(resultado.isEmpty());
  }

  // ========================================================
  // POST /api/cocina/comandas/sector/{idSector}/servir
  // ========================================================

  @Test
  @DisplayName(
    "HP-02 | servir | Devuelve 200 OK con ok=true cuando el servicio sirve el sector sin problemas"
  )
  void servirDeberiaDevolver200ConOkTrueCuandoElServicioNoTiraExcepcion() throws Exception {
    ResponseEntity<Map<String, Object>> respuesta = controlador.servir(10L);

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertEquals(true, respuesta.getBody().get("ok"));
    verify(servicioComanda).servirSector(10L);
  }

  @Test
  @DisplayName(
    "NEG-01 | servir | Devuelve 409 con el detalle de productos faltantes si faltan timers"
  )
  void servirDeberiaDevolver409ConProductosFaltantesSiFaltanTimers() throws Exception {
    List<Producto> faltantes = List.of(crearProducto(1L, "Queso"), crearProducto(2L, "Cebolla"));
    IngredientesNoDisponiblesException excepcion = new IngredientesNoDisponiblesException(
      faltantes
    );
    doThrow(excepcion).when(servicioComanda).servirSector(10L);

    ResponseEntity<Map<String, Object>> respuesta = controlador.servir(10L);

    assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    Map<String, Object> body = respuesta.getBody();
    assertEquals("faltan_timers", body.get("error"));
    assertEquals(excepcion.getMessage(), body.get("mensaje"));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> productos = (List<Map<String, Object>>) body.get("productos");
    assertEquals(2, productos.size());
    assertEquals(1L, productos.get(0).get("id"));
    assertEquals("Queso", productos.get(0).get("nombre"));
    assertEquals(2L, productos.get(1).get("id"));
    assertEquals("Cebolla", productos.get(1).get("nombre"));
  }

  @Test
  @DisplayName(
    "EDGE-02 | servir | Devuelve 409 con productos vacío si la excepción no trae faltantes"
  )
  void servirDeberiaDevolver409ConProductosVacioSiNoHayFaltantes() throws Exception {
    IngredientesNoDisponiblesException excepcion = new IngredientesNoDisponiblesException(
      List.of()
    );
    doThrow(excepcion).when(servicioComanda).servirSector(10L);

    ResponseEntity<Map<String, Object>> respuesta = controlador.servir(10L);

    assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> productos = (List<Map<String, Object>>) respuesta
      .getBody()
      .get("productos");
    assertTrue(productos.isEmpty());
  }
}
