package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Comanda;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.dominio.interfaces.ServicioComanda;
import com.tallerwebi.presentacion.controller.ControladorComandaCocinaApi;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControladorComandaCocinaApiTest {

  private ServicioComanda servicioComanda;
  private ControladorComandaCocinaApi controlador;

  @BeforeEach
  public void setUp() {
    servicioComanda = mock(ServicioComanda.class);
    controlador = new ControladorComandaCocinaApi(servicioComanda);
  }

  @Test
  public void queSePuedaListarComandasPorCategoria() {
    Comanda comanda = new Comanda();
    comanda.setId(1L);
    Pedido pedido = new Pedido();
    pedido.setId(10L);
    pedido.setDetalles(new ArrayList<>());
    comanda.setPedido(pedido);

    ComandaCocinaDTO dto = new ComandaCocinaDTO(comanda);
    when(servicioComanda.listarPendientesPorCategoria(1L)).thenReturn(Arrays.asList(dto));

    List<ComandaCocinaDTO> resultado = controlador.listarPorCategoria(1L);

    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getId(), equalTo(1L));
  }

  @Test
  public void queSePuedaSacarComandaExitosamente() throws IngredientesNoDisponiblesException {
    doNothing().when(servicioComanda).sacarComanda(1L);

    ResponseEntity<Map<String, Object>> response = controlador.sacar(1L);

    assertThat(response.getStatusCodeValue(), equalTo(200));
    assertThat(response.getBody().get("ok"), equalTo(true));
    verify(servicioComanda, times(1)).sacarComanda(1L);
  }

  @Test
  public void queDevuelvaErrorCuandoFaltanIngredientesAlSacarComanda()
    throws IngredientesNoDisponiblesException {
    Producto p1 = new Producto();
    p1.setId(1L);
    p1.setNombre("Carne");

    IngredientesNoDisponiblesException excepcion = new IngredientesNoDisponiblesException(
      Arrays.asList(p1)
    );
    doThrow(excepcion).when(servicioComanda).sacarComanda(1L);

    ResponseEntity<Map<String, Object>> response = controlador.sacar(1L);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.CONFLICT));
    assertThat(response.getBody().get("error"), equalTo("faltan_timers"));
    List<Map<String, Object>> productos = (List<Map<String, Object>>) response
      .getBody()
      .get("productos");
    assertThat(productos, hasSize(1));
    assertThat(productos.get(0).get("nombre"), equalTo("Carne"));
  }
}
