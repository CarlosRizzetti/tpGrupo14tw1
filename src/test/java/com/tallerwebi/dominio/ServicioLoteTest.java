package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.entity.ConsumoLote;
import com.tallerwebi.dominio.entity.Lote;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoLote;
import com.tallerwebi.dominio.excepcion.SinStockSuficienteException;
import com.tallerwebi.dominio.interfaces.RepositorioConsumoLote;
import com.tallerwebi.dominio.interfaces.RepositorioLote;
import com.tallerwebi.dominio.interfaces.RepositorioProducto;
import com.tallerwebi.dominio.services.ServicioLoteImpl;
import com.tallerwebi.presentacion.dto.NotificacionVencimientoDto;
import com.tallerwebi.presentacion.dto.StockProductoDTO;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServicioLoteTest {

  private RepositorioLote repositorioLote;
  private RepositorioConsumoLote repositorioConsumoLote;
  private RepositorioProducto repositorioProducto;
  private ServicioLoteImpl servicio;

  @BeforeEach
  public void init() {
    repositorioLote = mock(RepositorioLote.class);
    repositorioConsumoLote = mock(RepositorioConsumoLote.class);
    repositorioProducto = mock(RepositorioProducto.class);

    servicio = new ServicioLoteImpl(repositorioLote, repositorioConsumoLote, repositorioProducto);
  }

  // ---------- helpers ----------

  private Producto crearProducto(Long id, String nombre) {
    Producto producto = Producto.builder().id(id).nombre(nombre).build();
    return producto;
  }

  private Lote crearLote(
    Long id,
    Producto producto,
    Integer cantidadDisponible,
    EstadoLote estado,
    OffsetDateTime fechaVencimiento
  ) {
    Lote lote = new Lote();
    lote.setId(id);
    lote.setProducto(producto);
    lote.setCantidadInicial(cantidadDisponible);
    lote.setCantidadDisponible(cantidadDisponible);
    lote.setEstado(estado);
    lote.setFechaDeIngreso(OffsetDateTime.now().minusDays(30));
    lote.setFechaDeVencimiento(fechaVencimiento);
    lote.setProveedor("Proveedor Test");
    lote.setMarca("Marca Test");
    lote.setNumeroDeLote(1L);
    return lote;
  }

  // ========================================================
  // registrarLote
  // ========================================================

  @Test
  @DisplayName("NEG-01 | registrarLote | Lanza excepción si cantidadInicial es null")
  public void registrarLoteConCantidadInicialNulaDeberiaLanzarExcepcion() {
    Lote lote = new Lote();
    lote.setCantidadInicial(null);

    assertThrows(IllegalArgumentException.class, () -> servicio.registrarLote(lote));
    verifyNoInteractions(repositorioLote);
  }

  @Test
  @DisplayName("NEG-02 | registrarLote | Lanza excepción si cantidadInicial es cero o negativa")
  public void registrarLoteConCantidadInicialInvalidaDeberiaLanzarExcepcion() {
    Lote loteCero = new Lote();
    loteCero.setCantidadInicial(0);
    Lote loteNegativo = new Lote();
    loteNegativo.setCantidadInicial(-5);

    assertThrows(IllegalArgumentException.class, () -> servicio.registrarLote(loteCero));
    assertThrows(IllegalArgumentException.class, () -> servicio.registrarLote(loteNegativo));
    verifyNoInteractions(repositorioLote);
  }

  @Test
  @DisplayName(
    "HP-01 | registrarLote | Registra el lote seteando cantidadDisponible = cantidadInicial y lo guarda"
  )
  public void registrarLoteValidoDeberiaGuardarConCantidadDisponibleSeteada() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = new Lote();
    lote.setId(1L);
    lote.setProducto(producto);
    lote.setCantidadInicial(20);

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Collections.singletonList(lote));

    Lote resultado = servicio.registrarLote(lote);

    assertEquals(20, resultado.getCantidadDisponible());
    verify(repositorioLote, times(1)).guardar(lote);
  }

  @Test
  @DisplayName("HP-02 | registrarLote | Si es el único lote del producto, lo promueve a EN_USO")
  public void registrarLotePrimerLoteDelProductoDeberiaQuedarEnUso() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = new Lote();
    lote.setId(1L);
    lote.setProducto(producto);
    lote.setCantidadInicial(20);

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Collections.singletonList(lote));

    servicio.registrarLote(lote);

    assertEquals(EstadoLote.EN_USO, lote.getEstado());
    verify(repositorioLote, times(1)).actualizar(lote);
  }

  @Test
  @DisplayName(
    "HP-03 | registrarLote | Si vence antes que el EN_USO actual, lo destrona y toma su lugar"
  )
  public void registrarLoteQueVenceAntesDeberiaDestronarAlEnUsoActual() {
    Producto producto = crearProducto(1L, "Queso");
    Lote loteEnUsoActual = crearLote(
      1L,
      producto,
      10,
      EstadoLote.EN_USO,
      OffsetDateTime.now().plusDays(20)
    );
    Lote nuevoLote = crearLote(
      2L,
      producto,
      15,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(5)
    );

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Arrays.asList(nuevoLote, loteEnUsoActual)); // ya ordenado por vencimiento asc

    servicio.registrarLote(nuevoLote);

    assertEquals(EstadoLote.EN_USO, nuevoLote.getEstado());
    assertEquals(EstadoLote.DISPONIBLE, loteEnUsoActual.getEstado());
    verify(repositorioLote, times(1)).actualizar(nuevoLote);
    verify(repositorioLote, times(1)).actualizar(loteEnUsoActual);
  }

  @Test
  @DisplayName("HP-04 | registrarLote | Si vence después del EN_USO actual, no lo destrona")
  public void registrarLoteQueVenceDespuesNoDeberiaDestronarAlEnUsoActual() {
    Producto producto = crearProducto(1L, "Queso");
    Lote loteEnUsoActual = crearLote(
      1L,
      producto,
      10,
      EstadoLote.EN_USO,
      OffsetDateTime.now().plusDays(5)
    );
    Lote nuevoLote = crearLote(
      2L,
      producto,
      15,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(20)
    );

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Arrays.asList(loteEnUsoActual, nuevoLote)); // ya ordenado por vencimiento asc

    servicio.registrarLote(nuevoLote);

    assertEquals(EstadoLote.EN_USO, loteEnUsoActual.getEstado());
    assertEquals(EstadoLote.DISPONIBLE, nuevoLote.getEstado());
    verify(repositorioLote, never()).actualizar(loteEnUsoActual);
    verify(repositorioLote, never()).actualizar(nuevoLote);
  }

  @Test
  @DisplayName(
    "EDGE-01 | registrarLote | Si no hay lotes consumibles para reevaluar, no actualiza nada"
  )
  public void registrarLoteSinLotesConsumiblesNoDeberiaActualizarNada() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = new Lote();
    lote.setProducto(producto);
    lote.setCantidadInicial(20);

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(new ArrayList<>());

    servicio.registrarLote(lote);

    verify(repositorioLote, never()).actualizar(any());
  }

  // ========================================================
  // consumirCantidad
  // ========================================================

  @Test
  @DisplayName("NEG-01 | consumirCantidad | Lanza excepción si la cantidad necesaria es null")
  public void consumirCantidadNulaDeberiaLanzarExcepcion() {
    Producto producto = crearProducto(1L, "Queso");

    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.consumirCantidad(producto, null, new Timer())
    );
    verifyNoInteractions(repositorioLote, repositorioConsumoLote);
  }

  @Test
  @DisplayName(
    "NEG-02 | consumirCantidad | Lanza excepción si la cantidad necesaria es cero o negativa"
  )
  public void consumirCantidadInvalidaDeberiaLanzarExcepcion() {
    Producto producto = crearProducto(1L, "Queso");

    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.consumirCantidad(producto, 0, new Timer())
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> servicio.consumirCantidad(producto, -3, new Timer())
    );
    verifyNoInteractions(repositorioLote, repositorioConsumoLote);
  }

  @Test
  @DisplayName("NEG-03 | consumirCantidad | Lanza excepción si el stock total no alcanza")
  public void consumirCantidadSinStockSuficienteDeberiaLanzarExcepcion() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(1L, producto, 5, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(10));
    Timer timer = Timer.builder().build();

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Collections.singletonList(lote));

    assertThrows(
      SinStockSuficienteException.class,
      () -> servicio.consumirCantidad(producto, 10, timer)
    );

    // Se alcanzó a descontar lo que había en el lote antes de fallar
    assertEquals(0, lote.getCantidadDisponible());
    assertEquals(EstadoLote.CONSUMIDO, lote.getEstado());
    verify(repositorioConsumoLote, times(1)).guardar(any());
    // reevaluarFifo no debería llegar a ejecutarse: solo un llamado a listarConsumiblesDeProducto
    verify(repositorioLote, times(1)).listarConsumiblesDeProducto(producto.getId());
  }

  @Test
  @DisplayName("HP-01 | consumirCantidad | Descuenta de un único lote sin agotarlo")
  public void consumirCantidadDeUnLoteSinAgotarloDeberiaDescontarYNoCambiarEstado() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(1L, producto, 20, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(10));
    Timer timer = Timer.builder().build();

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Collections.singletonList(lote));

    List<ConsumoLote> consumos = servicio.consumirCantidad(producto, 8, timer);

    assertEquals(1, consumos.size());
    assertEquals(12, lote.getCantidadDisponible());
    assertEquals(EstadoLote.EN_USO, lote.getEstado());
    assertEquals(lote, consumos.get(0).getLote());
    assertEquals(timer, consumos.get(0).getTimer());
    assertEquals(8, consumos.get(0).getCantidadConsumida());
    verify(repositorioLote, times(1)).actualizar(lote);
    verify(repositorioConsumoLote, times(1)).guardar(consumos.get(0));
  }

  @Test
  @DisplayName("EDGE-02 | consumirCantidad | Si agota exactamente el lote, pasa a CONSUMIDO")
  public void consumirCantidadQueAgotaElLoteDeberiaMarcarloComoConsumido() {
    Producto producto = crearProducto(1L, "Queso");
    Lote loteA = crearLote(1L, producto, 10, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(10));
    Lote loteB = crearLote(
      2L,
      producto,
      20,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(15)
    );
    Timer timer = Timer.builder().build();
    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Arrays.asList(loteA, loteB), Collections.emptyList());

    servicio.consumirCantidad(producto, 10, timer);

    assertEquals(0, loteA.getCantidadDisponible());
    assertEquals(EstadoLote.CONSUMIDO, loteA.getEstado());
  }

  @Test
  @DisplayName("HP-02 | consumirCantidad | Si un lote no alcanza, sigue consumiendo del siguiente")
  public void consumirCantidadQueAbarcaVariosLotesDeberiaGenerarUnConsumoPorLote() {
    Producto producto = crearProducto(1L, "Queso");
    Lote loteA = crearLote(1L, producto, 5, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(5));
    Lote loteB = crearLote(
      2L,
      producto,
      20,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(15)
    );
    Timer timer = Timer.builder().build();
    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Arrays.asList(loteA, loteB), Collections.singletonList(loteB));

    List<ConsumoLote> consumos = servicio.consumirCantidad(producto, 12, timer);

    assertEquals(2, consumos.size());
    assertEquals(0, loteA.getCantidadDisponible());
    assertEquals(EstadoLote.CONSUMIDO, loteA.getEstado());
    assertEquals(13, loteB.getCantidadDisponible());
    assertEquals(EstadoLote.EN_USO, loteB.getEstado());
  }

  @Test
  @DisplayName(
    "EDGE-03 | consumirCantidad | Si el primer lote ya cubre todo, no toca los lotes sobrantes"
  )
  public void consumirCantidadCubiertaPorElPrimerLoteNoDeberiaTocarLosSiguientes() {
    Producto producto = crearProducto(1L, "Queso");
    Lote loteA = crearLote(1L, producto, 20, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(5));
    Lote loteB = crearLote(
      2L,
      producto,
      20,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(15)
    );
    Timer timer = Timer.builder().build();

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Arrays.asList(loteA, loteB));

    List<ConsumoLote> consumos = servicio.consumirCantidad(producto, 10, timer);

    assertEquals(1, consumos.size());
    assertEquals(10, loteA.getCantidadDisponible());
    assertEquals(20, loteB.getCantidadDisponible()); // sin tocar
    verify(repositorioLote, never()).actualizar(loteB);
  }

  @Test
  @DisplayName("EDGE-04 | consumirCantidad | Si un lote de la lista ya está en cero, lo saltea")
  public void consumirCantidadConLoteEnCeroEnLaListaDeberiaSaltearlo() {
    Producto producto = crearProducto(1L, "Queso");
    Lote loteVacio = crearLote(
      1L,
      producto,
      0,
      EstadoLote.EN_USO,
      OffsetDateTime.now().plusDays(5)
    );
    Lote loteA = crearLote(
      2L,
      producto,
      10,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(15)
    );
    Timer timer = Timer.builder().build();

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Arrays.asList(loteVacio, loteA));

    List<ConsumoLote> consumos = servicio.consumirCantidad(producto, 5, timer);

    assertEquals(1, consumos.size());
    assertEquals(loteA, consumos.get(0).getLote());
    assertEquals(5, loteA.getCantidadDisponible());
    verify(repositorioLote, never()).actualizar(loteVacio);
  }

  // ========================================================
  // stockDisponibleDe
  // ========================================================

  @Test
  @DisplayName(
    "HP-01 | stockDisponibleDe | Suma la cantidadDisponible de todos los lotes con stock"
  )
  public void stockDisponibleDeDeberiaSumarCantidadesDisponibles() {
    Producto producto = crearProducto(1L, "Queso");
    Lote loteA = crearLote(1L, producto, 10, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(5));
    Lote loteB = crearLote(
      2L,
      producto,
      15,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(15)
    );

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(Arrays.asList(loteA, loteB));

    Integer stock = servicio.stockDisponibleDe(producto);

    assertEquals(25, stock);
  }

  @Test
  @DisplayName("EDGE-01 | stockDisponibleDe | Devuelve 0 si no hay lotes con stock")
  public void stockDisponibleDeSinLotesDeberiaDevolverCero() {
    Producto producto = crearProducto(1L, "Queso");

    when(repositorioLote.listarConsumiblesDeProducto(producto.getId()))
      .thenReturn(new ArrayList<>());

    Integer stock = servicio.stockDisponibleDe(producto);

    assertEquals(0, stock);
  }

  // ========================================================
  // obtenerTodosLosLotes
  // ========================================================

  @Test
  @DisplayName("HP-01 | obtenerTodosLosLotes | Delega directo en el repositorio")
  public void obtenerTodosLosLotesDeberiaDelegarEnElRepositorio() {
    List<Lote> lotes = Collections.singletonList(new Lote());
    when(repositorioLote.listarTodos()).thenReturn(lotes);

    List<Lote> resultado = servicio.obtenerTodosLosLotes();

    assertEquals(lotes, resultado);
    verify(repositorioLote, times(1)).listarTodos();
  }

  // ========================================================
  // obtenerStockAgrupado
  // ========================================================

  @Test
  @DisplayName(
    "HP-01 | obtenerStockAgrupado | Arma total, lote en uso y disponibles ordenados por vencimiento"
  )
  public void obtenerStockAgrupadoDeberiaArmarElResumenPorProducto() {
    Producto producto = crearProducto(1L, "Queso");
    Lote enUso = crearLote(1L, producto, 10, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(2));
    Lote disponibleLejano = crearLote(
      2L,
      producto,
      15,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(20)
    );
    Lote disponibleCercano = crearLote(
      3L,
      producto,
      5,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(8)
    );
    Lote consumido = crearLote(
      4L,
      producto,
      0,
      EstadoLote.CONSUMIDO,
      OffsetDateTime.now().plusDays(1)
    );

    when(repositorioLote.listarTodos())
      .thenReturn(Arrays.asList(enUso, disponibleLejano, disponibleCercano, consumido));
    when(repositorioProducto.obtenerTodos()).thenReturn(Collections.singletonList(producto));

    List<StockProductoDTO> resultado = servicio.obtenerStockAgrupado();

    assertEquals(1, resultado.size());
    StockProductoDTO dto = resultado.get(0);
    assertEquals("Queso", dto.getNombreProducto());
    assertEquals(30, dto.getStockTotal()); // 10 + 15 + 5, sin contar el CONSUMIDO
    assertEquals(enUso, dto.getLoteEnUso());
    assertEquals(2, dto.getLotesDisponibles().size());
    assertEquals(disponibleCercano, dto.getLotesDisponibles().get(0)); // vence antes, va primero
    assertEquals(disponibleLejano, dto.getLotesDisponibles().get(1));
  }

  @Test
  @DisplayName("EDGE-01 | obtenerStockAgrupado | Un producto sin lotes aparece con stock 0")
  public void obtenerStockAgrupadoConProductoSinLotesDeberiaDevolverStockCero() {
    Producto producto = crearProducto(1L, "Cebolla");

    when(repositorioLote.listarTodos()).thenReturn(new ArrayList<>());
    when(repositorioProducto.obtenerTodos()).thenReturn(Collections.singletonList(producto));

    List<StockProductoDTO> resultado = servicio.obtenerStockAgrupado();

    assertEquals(1, resultado.size());
    StockProductoDTO dto = resultado.get(0);
    assertEquals(0, dto.getStockTotal());
    assertNull(dto.getLoteEnUso());
    assertTrue(dto.getLotesDisponibles().isEmpty());
  }

  @Test
  @DisplayName(
    "EDGE-02 | obtenerStockAgrupado | Un producto sin lote EN_USO devuelve loteEnUso null"
  )
  public void obtenerStockAgrupadoSinLoteEnUsoDeberiaDevolverLoteEnUsoNulo() {
    Producto producto = crearProducto(1L, "Tomate");
    Lote disponible = crearLote(
      1L,
      producto,
      10,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(5)
    );

    when(repositorioLote.listarTodos()).thenReturn(Collections.singletonList(disponible));
    when(repositorioProducto.obtenerTodos()).thenReturn(Collections.singletonList(producto));

    List<StockProductoDTO> resultado = servicio.obtenerStockAgrupado();

    assertNull(resultado.get(0).getLoteEnUso());
    assertEquals(1, resultado.get(0).getLotesDisponibles().size());
  }

  @Test
  @DisplayName("EDGE-03 | obtenerStockAgrupado | Lotes vencidos o descartados no suman stock")
  public void obtenerStockAgrupadoConLotesNoUtilizablesDeberiaDevolverStockCero() {
    Producto producto = crearProducto(1L, "Cafe");
    Lote vencido = crearLote(
      1L,
      producto,
      8,
      EstadoLote.VENCIDO,
      OffsetDateTime.now().minusDays(2)
    );
    Lote descartado = crearLote(
      2L,
      producto,
      3,
      EstadoLote.DESCARTADO,
      OffsetDateTime.now().plusDays(30)
    );

    when(repositorioLote.listarTodos()).thenReturn(Arrays.asList(vencido, descartado));
    when(repositorioProducto.obtenerTodos()).thenReturn(Collections.singletonList(producto));

    List<StockProductoDTO> resultado = servicio.obtenerStockAgrupado();

    StockProductoDTO dto = resultado.get(0);
    assertEquals(0, dto.getStockTotal());
    assertNull(dto.getLoteEnUso());
    assertTrue(dto.getLotesDisponibles().isEmpty());
  }

  // ========================================================
  // obtenerNotificacionesVencimiento
  // ========================================================

  @Test
  @DisplayName(
    "HP-01 | obtenerNotificacionesVencimiento | Lote a 2 días del vencimiento es urgencia ALTA"
  )
  public void obtenerNotificacionesConLoteAPocosDiasDeberiaSerUrgenciaAlta() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(1L, producto, 5, EstadoLote.EN_USO, OffsetDateTime.now().plusDays(2));

    when(repositorioLote.listarTodos()).thenReturn(Collections.singletonList(lote));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertEquals(1, notificaciones.size());
    assertEquals("ALTA", notificaciones.get(0).getNivelUrgencia());
    assertEquals(2, notificaciones.get(0).getDiasRestantes());
  }

  @Test
  @DisplayName(
    "HP-02 | obtenerNotificacionesVencimiento | Lote a 5 días del vencimiento es urgencia MEDIA"
  )
  public void obtenerNotificacionesConLoteAMedioPlazoDeberiaSerUrgenciaMedia() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(1L, producto, 5, EstadoLote.DISPONIBLE, OffsetDateTime.now().plusDays(5));

    when(repositorioLote.listarTodos()).thenReturn(Collections.singletonList(lote));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertEquals("MEDIA", notificaciones.get(0).getNivelUrgencia());
  }

  @Test
  @DisplayName(
    "HP-03 | obtenerNotificacionesVencimiento | Lote a 9 días del vencimiento es urgencia BAJA"
  )
  public void obtenerNotificacionesConLoteALargoPlazoDeberiaSerUrgenciaBaja() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(1L, producto, 5, EstadoLote.DISPONIBLE, OffsetDateTime.now().plusDays(9));

    when(repositorioLote.listarTodos()).thenReturn(Collections.singletonList(lote));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertEquals("BAJA", notificaciones.get(0).getNivelUrgencia());
  }

  @Test
  @DisplayName(
    "EDGE-01 | obtenerNotificacionesVencimiento | Lote que vence en más de 10 días no se notifica"
  )
  public void obtenerNotificacionesConLoteLejanoNoDeberiaNotificarlo() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(
      1L,
      producto,
      5,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(15)
    );

    when(repositorioLote.listarTodos()).thenReturn(Collections.singletonList(lote));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertTrue(notificaciones.isEmpty());
  }

  @Test
  @DisplayName(
    "EDGE-02 | obtenerNotificacionesVencimiento | Lote sin stock disponible no se notifica"
  )
  public void obtenerNotificacionesConLoteSinStockNoDeberiaNotificarlo() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(1L, producto, 0, EstadoLote.CONSUMIDO, OffsetDateTime.now().plusDays(2));

    when(repositorioLote.listarTodos()).thenReturn(Collections.singletonList(lote));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertTrue(notificaciones.isEmpty());
  }

  @Test
  @DisplayName(
    "EDGE-03 | obtenerNotificacionesVencimiento | Lote VENCIDO o DESCARTADO no se notifica"
  )
  public void obtenerNotificacionesConLoteNoUtilizableNoDeberiaNotificarlo() {
    Producto producto = crearProducto(1L, "Queso");
    Lote vencido = crearLote(1L, producto, 5, EstadoLote.VENCIDO, OffsetDateTime.now().plusDays(2));
    Lote descartado = crearLote(
      2L,
      producto,
      5,
      EstadoLote.DESCARTADO,
      OffsetDateTime.now().plusDays(2)
    );

    when(repositorioLote.listarTodos()).thenReturn(Arrays.asList(vencido, descartado));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertTrue(notificaciones.isEmpty());
  }

  @Test
  @DisplayName(
    "EDGE-04 | obtenerNotificacionesVencimiento | Lote sin fecha de vencimiento no se notifica"
  )
  public void obtenerNotificacionesConLoteSinFechaDeVencimientoNoDeberiaNotificarlo() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(1L, producto, 5, EstadoLote.DISPONIBLE, null);

    when(repositorioLote.listarTodos()).thenReturn(Collections.singletonList(lote));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertTrue(notificaciones.isEmpty());
  }

  @Test
  @DisplayName(
    "EDGE-05 | obtenerNotificacionesVencimiento | Lote ya vencido (días negativos) es urgencia ALTA"
  )
  public void obtenerNotificacionesConLoteYaVencidoDeberiaSerUrgenciaAlta() {
    Producto producto = crearProducto(1L, "Queso");
    Lote lote = crearLote(1L, producto, 5, EstadoLote.EN_USO, OffsetDateTime.now().minusDays(2));

    when(repositorioLote.listarTodos()).thenReturn(Collections.singletonList(lote));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertEquals(1, notificaciones.size());
    assertEquals("ALTA", notificaciones.get(0).getNivelUrgencia());
    assertEquals(-2, notificaciones.get(0).getDiasRestantes());
  }

  @Test
  @DisplayName(
    "HP-04 | obtenerNotificacionesVencimiento | Devuelve la lista ordenada por días restantes ascendente"
  )
  public void obtenerNotificacionesDeberiaOrdenarPorDiasRestantesAscendente() {
    Producto producto = crearProducto(1L, "Queso");
    Lote loteLejano = crearLote(
      1L,
      producto,
      5,
      EstadoLote.DISPONIBLE,
      OffsetDateTime.now().plusDays(6)
    );
    Lote loteCercano = crearLote(
      2L,
      producto,
      5,
      EstadoLote.EN_USO,
      OffsetDateTime.now().plusDays(1)
    );

    when(repositorioLote.listarTodos()).thenReturn(Arrays.asList(loteLejano, loteCercano));

    List<NotificacionVencimientoDto> notificaciones = servicio.obtenerNotificacionesVencimiento();

    assertEquals(2, notificaciones.size());
    assertEquals(1, notificaciones.get(0).getDiasRestantes());
    assertEquals(6, notificaciones.get(1).getDiasRestantes());
  }
}
