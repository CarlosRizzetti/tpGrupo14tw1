package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.interfaces.ServicioCliente;
import com.tallerwebi.dominio.interfaces.ServicioPedido;
import com.tallerwebi.dominio.interfaces.ServicioProductoFinal;
import com.tallerwebi.dominio.utils.CarritoPedido;
import com.tallerwebi.dominio.utils.ItemCarrito;
import com.tallerwebi.presentacion.controller.ControladorCajeroApi;
import com.tallerwebi.presentacion.dto.CarritoDTO;
import com.tallerwebi.presentacion.dto.ProductoFinalDTO;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class ControladorCajeroApiTest {

  private ServicioProductoFinal servicioProductoFinal;
  private ServicioPedido servicioPedido;
  private ServicioCliente servicioCliente;
  private HttpSession session;
  private ControladorCajeroApi controlador;
  private CarritoPedido carrito;

  @BeforeEach
  public void setUp() {
    servicioProductoFinal = mock(ServicioProductoFinal.class);
    servicioPedido = mock(ServicioPedido.class);
    servicioCliente = mock(ServicioCliente.class);
    session = mock(HttpSession.class);
    controlador = new ControladorCajeroApi(servicioProductoFinal, servicioPedido, servicioCliente);

    carrito = new CarritoPedido();
    when(session.getAttribute("carrito")).thenReturn(carrito);
  }

  @Test
  public void queSePuedaObtenerProductosPorCategoria() {
    ProductoFinal p1 = new ProductoFinal();
    p1.setId(1L);
    p1.setNombre("Hamburguesa");
    p1.setPrecio(BigDecimal.valueOf(100.0));

    when(servicioProductoFinal.listarPorCategoria(1L)).thenReturn(Arrays.asList(p1));

    List<ProductoFinalDTO> resultado = controlador.productosPorCategoria(1L);

    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getId(), equalTo(1L));
    assertThat(resultado.get(0).getNombre(), equalTo("Hamburguesa"));
  }

  @Test
  public void queSePuedaVerElCarrito() {
    CarritoDTO carritoDTO = controlador.verCarrito(session);
    assertThat(carritoDTO, notNullValue());
  }

  @Test
  public void queSePuedaAgregarUnItemAlCarrito() {
    ProductoFinal p1 = new ProductoFinal();
    p1.setId(1L);
    p1.setNombre("Hamburguesa");
    p1.setPrecio(BigDecimal.valueOf(100.0));
    when(servicioProductoFinal.buscarPorId(1L)).thenReturn(p1);

    ControladorCajeroApi.AgregarItemRequest request = new ControladorCajeroApi.AgregarItemRequest();
    request.setIdProductoFinal(1L);
    request.setIngredientesRetiradosIds(Arrays.asList(2L, 3L));

    CarritoDTO resultado = controlador.agregarItem(request, session);

    assertThat(resultado.getItems(), hasSize(1));
    assertThat(resultado.getItems().get(0).getNombre(), equalTo("Hamburguesa"));
    assertThat(carrito.getItems(), hasSize(1));
  }

  @Test
  public void queSePuedaEliminarUnItemDelCarrito() {
    ProductoFinal p1 = new ProductoFinal();
    p1.setId(1L);
    p1.setPrecio(BigDecimal.valueOf(100.0));
    ItemCarrito item = new ItemCarrito(p1);
    carrito.agregarItem(item);

    ResponseEntity<CarritoDTO> response = controlador.eliminarItem(0, session);

    assertThat(response.getStatusCodeValue(), equalTo(200));
    assertThat(carrito.getItems(), hasSize(0));
  }

  @Test
  public void queDevuelvaNotFoundAlEliminarItemInexistente() {
    ResponseEntity<CarritoDTO> response = controlador.eliminarItem(999, session);
    assertThat(response.getStatusCodeValue(), equalTo(404));
  }

  @Test
  public void queSePuedaBuscarUnCliente() {
    Cliente cliente = new Cliente();
    cliente.setId(1L);
    cliente.setDocumento("12345678");
    cliente.setNombre("Juan");

    when(servicioCliente.buscarPorDocumento("12345678")).thenReturn(cliente);

    Map<String, Object> respuesta = controlador.buscarCliente("12345678");

    assertThat(respuesta.get("cliente"), notNullValue());
    Map<String, Object> data = (Map<String, Object>) respuesta.get("cliente");
    assertThat(data.get("documento"), equalTo("12345678"));
  }

  @Test
  public void queDevuelvaNullAlBuscarClienteInexistente() {
    when(servicioCliente.buscarPorDocumento("12345678")).thenReturn(null);
    Map<String, Object> respuesta = controlador.buscarCliente("12345678");
    assertThat(respuesta.get("cliente"), nullValue());
  }

  @Test
  public void queSePuedaCobrarUnPedido() {
    ProductoFinal p1 = new ProductoFinal();
    p1.setId(1L);
    p1.setPrecio(BigDecimal.valueOf(100.0));
    carrito.agregarItem(new ItemCarrito(p1));

    Cliente cliente = new Cliente();
    when(servicioCliente.buscarPorDocumento("12345678")).thenReturn(cliente);

    Pedido pedido = new Pedido();
    pedido.setId(10L);
    pedido.setPrecioFinal(BigDecimal.valueOf(100.0));
    when(servicioPedido.cobrarPedido(carrito, cliente)).thenReturn(pedido);

    ResponseEntity<Map<String, Object>> response = controlador.cobrar("12345678", session);

    assertThat(response.getStatusCodeValue(), equalTo(200));
    assertThat(response.getBody().get("idPedido"), equalTo(10L));
    assertThat(response.getBody().get("precioFinal"), equalTo(BigDecimal.valueOf(100.0)));
    verify(session, times(1)).removeAttribute("carrito");
  }

  @Test
  public void queDevuelvaErrorAlCobrarCarritoVacio() {
    ResponseEntity<Map<String, Object>> response = controlador.cobrar("12345678", session);
    assertThat(response.getStatusCodeValue(), equalTo(400));
    assertThat(response.getBody().get("error"), equalTo("El carrito está vacío"));
  }
}
