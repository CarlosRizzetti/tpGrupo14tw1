package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.Cliente;
import com.tallerwebi.dominio.entity.ConsumoTimer;
import com.tallerwebi.dominio.entity.DetallePedido;
import com.tallerwebi.dominio.entity.DetallePedidoIngrediente;
import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.Pedido;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ProductoFinal;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.interfaces.RepositorioLote;
import com.tallerwebi.dominio.interfaces.RepositorioPedido;
import com.tallerwebi.dominio.services.ServicioHistorialPedidoImpl;
import com.tallerwebi.presentacion.dto.HistorialPedidoDTO;
import com.tallerwebi.presentacion.dto.IngredienteUsadoDTO;
import com.tallerwebi.presentacion.dto.ItemPedidoDTO;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServicioHistorialPedidoTest {

  private RepositorioPedido repositorioPedido;

  private RepositorioLote repositorioLote;

  private ServicioHistorialPedidoImpl servicioHistorialPedido;

  @BeforeEach
  void init() {
    this.repositorioPedido = mock(RepositorioPedido.class);
    this.repositorioLote = mock(RepositorioLote.class);
    servicioHistorialPedido = new ServicioHistorialPedidoImpl(repositorioPedido, repositorioLote);
  }

  // ---------- helpers ----------

  private Cliente crearCliente(Long id, String nombre) {
    Cliente cliente = new Cliente();
    cliente.setId(id);
    cliente.setNombre(nombre);
    return cliente;
  }

  private Timer crearTimer(Long id) {
    return Timer.builder().id(id).estado(EstadoTimer.ACTIVO).build();
  }

  private ConsumoTimer crearConsumo(Timer timer) {
    ConsumoTimer consumo = new ConsumoTimer();
    consumo.setTimer(timer);
    return consumo;
  }

  private DetallePedidoIngrediente crearIngrediente(
    String nombreProducto,
    Integer cantidad,
    List<ConsumoTimer> consumos
  ) {
    Producto producto = new Producto();
    producto.setNombre(nombreProducto);
    DetallePedidoIngrediente ingrediente = new DetallePedidoIngrediente();
    ingrediente.setProducto(producto);
    ingrediente.setCantidad(cantidad);
    ingrediente.setConsumos(consumos);
    return ingrediente;
  }

  private DetallePedido crearDetalle(
    String nombreProductoFinal,
    List<DetallePedidoIngrediente> ingredientes
  ) {
    ProductoFinal productoFinal = new ProductoFinal();
    productoFinal.setNombre(nombreProductoFinal);
    DetallePedido detalle = new DetallePedido();
    detalle.setProductoFinal(productoFinal);
    detalle.setIngredientes(ingredientes);
    return detalle;
  }

  private Pedido crearPedido(
    Long id,
    Cliente cliente,
    OffsetDateTime horaCobro,
    List<DetallePedido> detalles
  ) {
    Pedido pedido = new Pedido();
    pedido.setId(id);
    pedido.setCliente(cliente);
    pedido.setHoraCobro(horaCobro);
    pedido.setDetalles(detalles);
    return pedido;
  }

  // ========================================================
  // buscarHistorial - filtros
  // ========================================================

  @Test
  @DisplayName("HP-01 | buscarHistorial | Sin filtros, devuelve todos los pedidos mapeados")
  void buscarHistorialSinFiltrosDevuelveTodosLosPedidos() {
    Cliente cliente = crearCliente(1L, "Juan Pérez");
    Pedido pedidoUno = crearPedido(1L, cliente, OffsetDateTime.now(), List.of());
    Pedido pedidoDos = crearPedido(2L, cliente, OffsetDateTime.now(), List.of());
    when(repositorioPedido.listarTodos()).thenReturn(Arrays.asList(pedidoUno, pedidoDos));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      null,
      null,
      null,
      null
    );

    assertThat(resultado, hasSize(2));
  }

  @Test
  @DisplayName("HP-02 | buscarHistorial | Filtra los pedidos fuera del rango de fechas")
  void buscarHistorialFiltraPorRangoDeFechas() {
    OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.ofHours(-3));
    Pedido dentroDelRango = crearPedido(1L, null, ahora, List.of());
    Pedido fueraDelRango = crearPedido(2L, null, ahora.minusDays(5), List.of());
    when(repositorioPedido.listarTodos()).thenReturn(Arrays.asList(dentroDelRango, fueraDelRango));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      ahora.minusHours(1),
      ahora.plusHours(1),
      null,
      null
    );

    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getId(), equalTo(1L));
  }

  @Test
  @DisplayName(
    "EDGE-01 | buscarHistorial | Excluye pedidos sin horaCobro cuando hay filtro de fecha"
  )
  void buscarHistorialExcluyePedidosSinHoraCobroSiHayFiltroDeFecha() {
    OffsetDateTime ahora = OffsetDateTime.now();
    Pedido sinHoraCobro = crearPedido(1L, null, null, List.of());
    when(repositorioPedido.listarTodos()).thenReturn(List.of(sinHoraCobro));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      ahora.minusDays(1),
      ahora.plusDays(1),
      null,
      null
    );

    assertThat(resultado, empty());
  }

  @Test
  @DisplayName(
    "HP-03 | buscarHistorial | Filtra por nombre de cliente sin distinguir mayúsculas y por coincidencia parcial"
  )
  void buscarHistorialFiltraPorNombreDeClienteParcialYSinDistinguirMayusculas() {
    Cliente juan = crearCliente(1L, "Juan Pérez");
    Cliente ana = crearCliente(2L, "Ana Gómez");
    Pedido pedidoDeJuan = crearPedido(1L, juan, OffsetDateTime.now(), List.of());
    Pedido pedidoDeAna = crearPedido(2L, ana, OffsetDateTime.now(), List.of());
    when(repositorioPedido.listarTodos()).thenReturn(Arrays.asList(pedidoDeJuan, pedidoDeAna));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      null,
      null,
      null,
      "pérez"
    );

    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getClienteNombre(), equalTo("Juan Pérez"));
  }

  @Test
  @DisplayName(
    "NEG-01 | buscarHistorial | Excluye pedidos sin cliente asociado cuando hay filtro por nombre de cliente"
  )
  void buscarHistorialExcluyePedidosSinClienteSiHayFiltroPorNombre() {
    Pedido sinCliente = crearPedido(1L, null, OffsetDateTime.now(), List.of());
    when(repositorioPedido.listarTodos()).thenReturn(List.of(sinCliente));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      null,
      null,
      null,
      "juan"
    );

    assertThat(resultado, empty());
  }

  @Test
  @DisplayName(
    "HP-04 | buscarHistorial | Filtra por número de lote consumido en algún ingrediente del pedido"
  )
  void buscarHistorialFiltraPorNumeroDeLoteConsumido() {
    Timer timer = crearTimer(10L);
    DetallePedidoIngrediente ingrediente = crearIngrediente(
      "Queso",
      2,
      List.of(crearConsumo(timer))
    );
    DetallePedido detalle = crearDetalle("Hamburguesa Completa", List.of(ingrediente));
    Pedido pedidoConLote = crearPedido(1L, null, OffsetDateTime.now(), List.of(detalle));
    Pedido pedidoSinLote = crearPedido(2L, null, OffsetDateTime.now(), List.of());
    when(repositorioPedido.listarTodos()).thenReturn(Arrays.asList(pedidoConLote, pedidoSinLote));

    Lote lote = new Lote();
    lote.setNumeroDeLote(555L);
    when(repositorioLote.obtenerLotesPorTimer(10L)).thenReturn(List.of(lote));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      null,
      null,
      555L,
      null
    );

    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getId(), equalTo(1L));
  }

  @Test
  @DisplayName(
    "NEG-02 | buscarHistorial | Devuelve vacío si ningún pedido consumió el número de lote buscado"
  )
  void buscarHistorialDevuelveVacioSiNingunPedidoConsumioEseLote() {
    Timer timer = crearTimer(10L);
    DetallePedidoIngrediente ingrediente = crearIngrediente(
      "Queso",
      2,
      List.of(crearConsumo(timer))
    );
    DetallePedido detalle = crearDetalle("Hamburguesa Completa", List.of(ingrediente));
    Pedido pedido = crearPedido(1L, null, OffsetDateTime.now(), List.of(detalle));
    when(repositorioPedido.listarTodos()).thenReturn(List.of(pedido));

    Lote lote = new Lote();
    lote.setNumeroDeLote(555L);
    when(repositorioLote.obtenerLotesPorTimer(10L)).thenReturn(List.of(lote));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      null,
      null,
      999L,
      null
    );

    assertThat(resultado, empty());
  }

  // ========================================================
  // buscarHistorial - mapeo
  // ========================================================

  @Test
  @DisplayName(
    "HP-05 | buscarHistorial | Mapea correctamente items, ingredientes y los ids de timers usados"
  )
  void buscarHistorialMapeaItemsIngredientesYTimers() {
    Timer timerUno = crearTimer(10L);
    Timer timerDos = crearTimer(11L);
    DetallePedidoIngrediente ingrediente = crearIngrediente(
      "Queso",
      2,
      Arrays.asList(crearConsumo(timerUno), crearConsumo(timerDos))
    );
    DetallePedido detalle = crearDetalle("Hamburguesa Completa", List.of(ingrediente));
    Pedido pedido = crearPedido(1L, null, OffsetDateTime.now(), List.of(detalle));
    when(repositorioPedido.listarTodos()).thenReturn(List.of(pedido));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      null,
      null,
      null,
      null
    );

    ItemPedidoDTO item = resultado.get(0).getItems().get(0);
    assertThat(item.getNombreProducto(), equalTo("Hamburguesa Completa"));
    IngredienteUsadoDTO ingredienteDto = item.getIngredientes().get(0);
    assertThat(ingredienteDto.getNombreProducto(), equalTo("Queso"));
    assertThat(ingredienteDto.getCantidad(), equalTo(2));
    assertThat(ingredienteDto.getTimers(), containsInAnyOrder(10L, 11L));
  }

  @Test
  @DisplayName(
    "NEG-03 | buscarHistorial | Mapea \"Sin cliente\" y clienteId null cuando el pedido no tiene cliente asociado"
  )
  void buscarHistorialMapeaSinClienteCuandoNoHayClienteAsociado() {
    Pedido pedido = crearPedido(1L, null, OffsetDateTime.now(), List.of());
    when(repositorioPedido.listarTodos()).thenReturn(List.of(pedido));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarHistorial(
      null,
      null,
      null,
      null
    );

    assertThat(resultado.get(0).getClienteId(), nullValue());
    assertThat(resultado.get(0).getClienteNombre(), equalTo("Sin cliente"));
  }

  // ========================================================
  // buscarPorCliente
  // ========================================================

  @Test
  @DisplayName(
    "HP-06 | buscarPorCliente | Devuelve los pedidos del cliente, mapeados con el mismo formato del historial"
  )
  void buscarPorClienteDevuelveLosPedidosDelClienteMapeados() {
    Cliente cliente = crearCliente(1L, "Juan Pérez");
    Pedido pedido = crearPedido(1L, cliente, OffsetDateTime.now(), List.of());
    when(repositorioPedido.listarPorCliente(1L)).thenReturn(List.of(pedido));

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarPorCliente(1L);

    assertThat(resultado, hasSize(1));
    assertThat(resultado.get(0).getClienteNombre(), equalTo("Juan Pérez"));
    verify(repositorioPedido).listarPorCliente(1L);
  }

  @Test
  @DisplayName("EDGE-02 | buscarPorCliente | Devuelve vacío si el cliente no tiene pedidos")
  void buscarPorClienteDevuelveVacioSiNoTienePedidos() {
    when(repositorioPedido.listarPorCliente(1L)).thenReturn(List.of());

    List<HistorialPedidoDTO> resultado = servicioHistorialPedido.buscarPorCliente(1L);

    assertThat(resultado, empty());
  }
}
