package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.entity.*;
import com.tallerwebi.dominio.entity.enums.EstadoComanda;
import com.tallerwebi.dominio.entity.enums.EstadoComandaSector;
import com.tallerwebi.dominio.entity.enums.EstadoPedido;
import com.tallerwebi.dominio.excepcion.IngredientesNoDisponiblesException;
import com.tallerwebi.dominio.interfaces.RepositorioComanda;
import com.tallerwebi.dominio.interfaces.RepositorioComandaSector;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.dominio.services.ServicioComandaImpl;
import com.tallerwebi.presentacion.dto.ComandaCocinaDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioComandaImplTest {

  private RepositorioComanda repositorioComanda;
  private RepositorioComandaSector repositorioComandaSector;
  private ServicioTimer servicioTimer;
  private ServicioComandaImpl servicioComanda;

  @BeforeEach
  void init() {
    repositorioComanda = mock(RepositorioComanda.class);
    repositorioComandaSector = mock(RepositorioComandaSector.class);
    servicioTimer = mock(ServicioTimer.class);
    servicioComanda =
      new ServicioComandaImpl(repositorioComanda, repositorioComandaSector, servicioTimer);
  }

  // ---------- helpers ----------

  private Categoria crearCategoria(Long id, String nombre) {
    Categoria categoria = new Categoria();
    categoria.setId(id);
    categoria.setNombre(nombre);
    return categoria;
  }

  private ProductoFinal crearProductoFinalConCategorias(String nombre, Categoria... categorias) {
    ProductoFinal productoFinal = new ProductoFinal(nombre, new BigDecimal("100.00"));
    for (Categoria categoria : categorias) {
      productoFinal.getCategorias().add(categoria);
    }
    return productoFinal;
  }

  private DetallePedido crearDetalle(
    ProductoFinal productoFinal,
    List<DetallePedidoIngrediente> ingredientes
  ) {
    DetallePedido detalle = new DetallePedido();
    detalle.setProductoFinal(productoFinal);
    detalle.setIngredientes(ingredientes);
    return detalle;
  }

  private Pedido crearPedido(List<DetallePedido> detalles) {
    Pedido pedido = new Pedido();
    pedido.setDetalles(detalles);
    return pedido;
  }

  private Comanda crearComanda(Pedido pedido) {
    Comanda comanda = new Comanda();
    comanda.setPedido(pedido);
    return comanda;
  }

  private ComandaSector crearSector(
    Comanda comanda,
    Categoria categoria,
    EstadoComandaSector estado
  ) {
    ComandaSector sector = new ComandaSector();
    sector.setComanda(comanda);
    sector.setCategoria(categoria);
    sector.setEstado(estado);
    comanda.getSectores().add(sector);
    return sector;
  }

  private DetallePedidoIngrediente crearIngrediente(
    Long idProducto,
    String nombreProducto,
    int cantidad,
    List<ConsumoTimer> consumos
  ) {
    Producto producto = new Producto();
    producto.setId(idProducto);
    producto.setNombre(nombreProducto);
    DetallePedidoIngrediente ingrediente = new DetallePedidoIngrediente();
    ingrediente.setProducto(producto);
    ingrediente.setCantidad(cantidad);
    ingrediente.setConsumos(consumos != null ? consumos : new ArrayList<>());
    return ingrediente;
  }

  private Timer crearTimer(Long id, int cantidadProducto) {
    return Timer.builder().id(id).cantidadProducto(cantidadProducto).build();
  }

  // ========================================================
  // crearSectoresDeComanda
  // ========================================================

  @Test
  @DisplayName(
    "HP-01 | crearSectoresDeComanda | Si el pedido no tiene productos de \"Cocina\", todos los sectores arrancan PENDIENTE"
  )
  void crearSectoresDeComandaSinCocinaTodosArrancanPendiente() {
    Categoria bebidas = crearCategoria(1L, "Bebidas");
    Categoria postres = crearCategoria(2L, "Postres");
    ProductoFinal gaseosa = crearProductoFinalConCategorias("Gaseosa", bebidas);
    ProductoFinal flan = crearProductoFinalConCategorias("Flan", postres);
    Pedido pedido = crearPedido(
      List.of(crearDetalle(gaseosa, List.of()), crearDetalle(flan, List.of()))
    );
    Comanda comanda = crearComanda(pedido);

    servicioComanda.crearSectoresDeComanda(comanda);

    ArgumentCaptor<ComandaSector> captor = ArgumentCaptor.forClass(ComandaSector.class);
    verify(repositorioComandaSector, times(2)).guardar(captor.capture());
    assertTrue(
      captor.getAllValues().stream().allMatch(s -> s.getEstado() == EstadoComandaSector.PENDIENTE)
    );
  }

  @Test
  @DisplayName(
    "HP-02 | crearSectoresDeComanda | Si el pedido tiene \"Cocina\", ese sector arranca PENDIENTE y el resto BLOQUEADO"
  )
  void crearSectoresDeComandaConCocinaSoloCocinaArrancaPendiente() {
    Categoria cocina = crearCategoria(1L, "Cocina");
    Categoria bebidas = crearCategoria(2L, "Bebidas");
    ProductoFinal hamburguesa = crearProductoFinalConCategorias("Hamburguesa", cocina);
    ProductoFinal gaseosa = crearProductoFinalConCategorias("Gaseosa", bebidas);
    Pedido pedido = crearPedido(
      List.of(crearDetalle(hamburguesa, List.of()), crearDetalle(gaseosa, List.of()))
    );
    Comanda comanda = crearComanda(pedido);

    servicioComanda.crearSectoresDeComanda(comanda);

    ArgumentCaptor<ComandaSector> captor = ArgumentCaptor.forClass(ComandaSector.class);
    verify(repositorioComandaSector, times(2)).guardar(captor.capture());
    Map<String, EstadoComandaSector> estadoPorCategoria = captor
      .getAllValues()
      .stream()
      .collect(Collectors.toMap(s -> s.getCategoria().getNombre(), ComandaSector::getEstado));

    assertEquals(EstadoComandaSector.PENDIENTE, estadoPorCategoria.get("Cocina"));
    assertEquals(EstadoComandaSector.BLOQUEADO, estadoPorCategoria.get("Bebidas"));
  }

  @Test
  @DisplayName(
    "EDGE-01 | crearSectoresDeComanda | No duplica el sector si dos productos comparten la misma categoría"
  )
  void crearSectoresDeComandaNoDuplicaSectorSiComparteCategoria() {
    Categoria cocina = crearCategoria(1L, "Cocina");
    ProductoFinal hamburguesa = crearProductoFinalConCategorias("Hamburguesa", cocina);
    ProductoFinal papas = crearProductoFinalConCategorias("Papas", cocina);
    Pedido pedido = crearPedido(
      List.of(crearDetalle(hamburguesa, List.of()), crearDetalle(papas, List.of()))
    );
    Comanda comanda = crearComanda(pedido);

    servicioComanda.crearSectoresDeComanda(comanda);

    verify(repositorioComandaSector, times(1)).guardar(any());
  }

  // ========================================================
  // servirSector
  // ========================================================

  @Test
  @DisplayName(
    "HP-03 | servirSector | Descuenta stock de los ingredientes del sector y lo marca SERVIDO"
  )
  void servirSectorDescuentaStockYMarcaServido() throws IngredientesNoDisponiblesException {
    Categoria bebidas = crearCategoria(1L, "Bebidas");
    ProductoFinal gaseosa = crearProductoFinalConCategorias("Gaseosa", bebidas);
    DetallePedidoIngrediente hielo = crearIngrediente(10L, "Hielo", 2, null);
    Pedido pedido = crearPedido(List.of(crearDetalle(gaseosa, List.of(hielo))));
    Comanda comanda = crearComanda(pedido);
    ComandaSector sector = crearSector(comanda, bebidas, EstadoComandaSector.PENDIENTE);
    sector.setId(99L);

    when(repositorioComandaSector.buscarPorId(99L)).thenReturn(sector);
    Timer timer = crearTimer(5L, 10);
    when(servicioTimer.obtenerTimersActivosConStockPorProducto(10L)).thenReturn(List.of(timer));

    servicioComanda.servirSector(99L);

    verify(servicioTimer).descontarStock(5L, 2);
    assertEquals(EstadoComandaSector.SERVIDO, sector.getEstado());
    assertNotNull(sector.getHoraServido());
    verify(repositorioComandaSector).actualizar(sector);
  }

  @Test
  @DisplayName(
    "HP-04 | servirSector | Si el sector servido es \"Cocina\", desbloquea los sectores BLOQUEADOS de la misma comanda"
  )
  void servirSectorCocinaDesbloqueaOtrosSectores() throws IngredientesNoDisponiblesException {
    Categoria cocina = crearCategoria(1L, "Cocina");
    Categoria bebidas = crearCategoria(2L, "Bebidas");
    ProductoFinal hamburguesa = crearProductoFinalConCategorias("Hamburguesa", cocina);
    Pedido pedido = crearPedido(List.of(crearDetalle(hamburguesa, List.of())));
    Comanda comanda = crearComanda(pedido);
    ComandaSector sectorCocina = crearSector(comanda, cocina, EstadoComandaSector.PENDIENTE);
    sectorCocina.setId(1L);
    ComandaSector sectorBebidas = crearSector(comanda, bebidas, EstadoComandaSector.BLOQUEADO);

    when(repositorioComandaSector.buscarPorId(1L)).thenReturn(sectorCocina);

    servicioComanda.servirSector(1L);

    assertEquals(EstadoComandaSector.PENDIENTE, sectorBebidas.getEstado());
    verify(repositorioComandaSector).actualizar(sectorBebidas);
  }

  @Test
  @DisplayName(
    "NEG-01 | servirSector | Si el sector servido NO es \"Cocina\", no toca los sectores BLOQUEADOS de la comanda"
  )
  void servirSectorNoCocinaNoDesbloqueaOtrosSectores() throws IngredientesNoDisponiblesException {
    Categoria cocina = crearCategoria(1L, "Cocina");
    Categoria bebidas = crearCategoria(2L, "Bebidas");
    Categoria postres = crearCategoria(3L, "Postres");
    ProductoFinal gaseosa = crearProductoFinalConCategorias("Gaseosa", bebidas);
    Pedido pedido = crearPedido(List.of(crearDetalle(gaseosa, List.of())));
    Comanda comanda = crearComanda(pedido);
    ComandaSector sectorCocina = crearSector(comanda, cocina, EstadoComandaSector.BLOQUEADO);
    ComandaSector sectorBebidas = crearSector(comanda, bebidas, EstadoComandaSector.PENDIENTE);
    sectorBebidas.setId(2L);
    ComandaSector sectorPostres = crearSector(comanda, postres, EstadoComandaSector.BLOQUEADO);

    when(repositorioComandaSector.buscarPorId(2L)).thenReturn(sectorBebidas);

    servicioComanda.servirSector(2L);

    assertEquals(EstadoComandaSector.BLOQUEADO, sectorCocina.getEstado());
    assertEquals(EstadoComandaSector.BLOQUEADO, sectorPostres.getEstado());
    verify(repositorioComandaSector, never()).actualizar(sectorCocina);
    verify(repositorioComandaSector, never()).actualizar(sectorPostres);
  }

  @Test
  @DisplayName(
    "HP-05 | servirSector | Finaliza el pedido cuando el sector servido era el último pendiente"
  )
  void servirSectorFinalizaPedidoCuandoEraElUltimoSector()
    throws IngredientesNoDisponiblesException {
    Categoria bebidas = crearCategoria(1L, "Bebidas");
    ProductoFinal gaseosa = crearProductoFinalConCategorias("Gaseosa", bebidas);
    Pedido pedido = crearPedido(List.of(crearDetalle(gaseosa, List.of())));
    Comanda comanda = crearComanda(pedido);
    crearSector(comanda, crearCategoria(2L, "Cocina"), EstadoComandaSector.SERVIDO);
    ComandaSector sectorBebidas = crearSector(comanda, bebidas, EstadoComandaSector.PENDIENTE);
    sectorBebidas.setId(2L);

    when(repositorioComandaSector.buscarPorId(2L)).thenReturn(sectorBebidas);

    servicioComanda.servirSector(2L);

    assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
    assertNotNull(pedido.getHoraSalida());
    assertEquals(EstadoComanda.SACADA, comanda.getEstado());
    verify(repositorioComanda).actualizar(comanda);
  }

  @Test
  @DisplayName(
    "NEG-02 | servirSector | No finaliza el pedido si todavía quedan sectores sin servir"
  )
  void servirSectorNoFinalizaPedidoSiQuedanSectoresPendientes()
    throws IngredientesNoDisponiblesException {
    Categoria bebidas = crearCategoria(1L, "Bebidas");
    Categoria postres = crearCategoria(2L, "Postres");
    ProductoFinal gaseosa = crearProductoFinalConCategorias("Gaseosa", bebidas);
    Pedido pedido = crearPedido(List.of(crearDetalle(gaseosa, List.of())));
    Comanda comanda = crearComanda(pedido);
    ComandaSector sectorBebidas = crearSector(comanda, bebidas, EstadoComandaSector.PENDIENTE);
    sectorBebidas.setId(1L);
    crearSector(comanda, postres, EstadoComandaSector.PENDIENTE);

    when(repositorioComandaSector.buscarPorId(1L)).thenReturn(sectorBebidas);

    servicioComanda.servirSector(1L);

    assertNull(pedido.getEstado());
    verify(repositorioComanda, never()).actualizar(any());
  }

  @Test
  @DisplayName(
    "NEG-03 | servirSector | Lanza IngredientesNoDisponiblesException si faltan timers y no marca el sector como servido"
  )
  void servirSectorLanzaExcepcionSiFaltanIngredientesYNoMarcaServido() {
    Categoria bebidas = crearCategoria(1L, "Bebidas");
    ProductoFinal gaseosa = crearProductoFinalConCategorias("Gaseosa", bebidas);
    DetallePedidoIngrediente hielo = crearIngrediente(10L, "Hielo", 5, null);
    Pedido pedido = crearPedido(List.of(crearDetalle(gaseosa, List.of(hielo))));
    Comanda comanda = crearComanda(pedido);
    ComandaSector sector = crearSector(comanda, bebidas, EstadoComandaSector.PENDIENTE);
    sector.setId(1L);

    when(repositorioComandaSector.buscarPorId(1L)).thenReturn(sector);
    when(servicioTimer.obtenerTimersActivosConStockPorProducto(10L)).thenReturn(List.of());

    IngredientesNoDisponiblesException excepcion = assertThrows(
      IngredientesNoDisponiblesException.class,
      () -> servicioComanda.servirSector(1L)
    );

    assertEquals(1, excepcion.getFaltantes().size());
    assertEquals("Hielo", excepcion.getFaltantes().get(0).getNombre());
    assertEquals(EstadoComandaSector.PENDIENTE, sector.getEstado());
    verify(repositorioComandaSector, never()).actualizar(any());
    verify(servicioTimer, never()).descontarStock(any(), anyInt());
  }

  @Test
  @DisplayName(
    "EDGE-02 | servirSector | No vuelve a descontar un ingrediente que ya fue consumido por otro sector"
  )
  void servirSectorNoDescuentaIngredienteYaConsumido() throws IngredientesNoDisponiblesException {
    Categoria cocina = crearCategoria(1L, "Cocina");
    Categoria bebidas = crearCategoria(2L, "Bebidas");
    ProductoFinal combo = crearProductoFinalConCategorias("Combo", cocina, bebidas);
    ConsumoTimer consumoPrevio = new ConsumoTimer();
    DetallePedidoIngrediente hielo = crearIngrediente(10L, "Hielo", 2, List.of(consumoPrevio));
    Pedido pedido = crearPedido(List.of(crearDetalle(combo, List.of(hielo))));
    Comanda comanda = crearComanda(pedido);
    ComandaSector sectorBebidas = crearSector(comanda, bebidas, EstadoComandaSector.PENDIENTE);
    sectorBebidas.setId(1L);

    when(repositorioComandaSector.buscarPorId(1L)).thenReturn(sectorBebidas);

    servicioComanda.servirSector(1L);

    verify(servicioTimer, never()).obtenerTimersActivosConStockPorProducto(any());
    verify(servicioTimer, never()).descontarStock(any(), anyInt());
    assertEquals(EstadoComandaSector.SERVIDO, sectorBebidas.getEstado());
  }

  @Test
  @DisplayName(
    "EDGE-03 | servirSector | Sirve sin problemas un sector cuyos productos no tienen ingredientes personalizables"
  )
  void servirSectorSinIngredientesSirveSinLlamarAlServicioDeTimers()
    throws IngredientesNoDisponiblesException {
    Categoria postres = crearCategoria(1L, "Postres");
    ProductoFinal flan = crearProductoFinalConCategorias("Flan", postres);
    Pedido pedido = crearPedido(List.of(crearDetalle(flan, List.of())));
    Comanda comanda = crearComanda(pedido);
    ComandaSector sector = crearSector(comanda, postres, EstadoComandaSector.PENDIENTE);
    sector.setId(1L);

    when(repositorioComandaSector.buscarPorId(1L)).thenReturn(sector);

    servicioComanda.servirSector(1L);

    verify(servicioTimer, never()).descontarStock(any(), anyInt());
    assertEquals(EstadoComandaSector.SERVIDO, sector.getEstado());
  }

  // ========================================================
  // listarPendientesPorCategoria / contarPendientesPorCategoria
  // ========================================================

  @Test
  @DisplayName(
    "HP-06 | listarPendientesPorCategoria | Mapea los ComandaSector visibles del repositorio a ComandaCocinaDTO"
  )
  void listarPendientesPorCategoriaMapeaLosSectoresVisibles() {
    Categoria bebidas = crearCategoria(1L, "Bebidas");
    ProductoFinal gaseosa = crearProductoFinalConCategorias("Gaseosa", bebidas);
    Pedido pedido = crearPedido(List.of(crearDetalle(gaseosa, List.of())));
    pedido.setId(50L);
    Comanda comanda = crearComanda(pedido);
    comanda.setId(7L);
    ComandaSector sector = crearSector(comanda, bebidas, EstadoComandaSector.PENDIENTE);
    sector.setId(3L);

    when(repositorioComandaSector.listarVisiblesPorCategoria(1L)).thenReturn(List.of(sector));

    List<ComandaCocinaDTO> resultado = servicioComanda.listarPendientesPorCategoria(1L);

    assertEquals(1, resultado.size());
    assertEquals(3L, resultado.get(0).getIdSector());
    assertEquals(50L, resultado.get(0).getIdPedido());
  }

  @Test
  @DisplayName(
    "EDGE-04 | listarPendientesPorCategoria | Devuelve una lista vacía si no hay sectores visibles"
  )
  void listarPendientesPorCategoriaDevuelveVacioSiNoHaySectores() {
    when(repositorioComandaSector.listarVisiblesPorCategoria(1L)).thenReturn(List.of());

    List<ComandaCocinaDTO> resultado = servicioComanda.listarPendientesPorCategoria(1L);

    assertTrue(resultado.isEmpty());
  }

  @Test
  @DisplayName("HP-07 | contarPendientesPorCategoria | Devuelve la cantidad de sectores visibles")
  void contarPendientesPorCategoriaDevuelveLaCantidad() {
    when(repositorioComandaSector.listarVisiblesPorCategoria(1L))
      .thenReturn(List.of(new ComandaSector(), new ComandaSector()));

    int resultado = servicioComanda.contarPendientesPorCategoria(1L);

    assertEquals(2, resultado);
  }

  @Test
  @DisplayName("EDGE-05 | contarPendientesPorCategoria | Devuelve 0 si no hay sectores visibles")
  void contarPendientesPorCategoriaDevuelveCeroSiNoHaySectores() {
    when(repositorioComandaSector.listarVisiblesPorCategoria(1L)).thenReturn(List.of());

    int resultado = servicioComanda.contarPendientesPorCategoria(1L);

    assertEquals(0, resultado);
  }
}
