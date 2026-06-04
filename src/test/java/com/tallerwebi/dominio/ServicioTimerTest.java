package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.excepcion.IdInvalido;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.services.ServicioTimerImpl;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioTimerTest {

  public RepositorioTimer repositorioTimerMock;
  public RepositorioCategoria repositorioCategoriaMock;
  public ServicioTimerImpl servicioTimer;
  public ServicioReglaVencimiento servicioReglaVencimientoMock;

  @BeforeEach
  public void init() {
    this.repositorioTimerMock = mock(RepositorioTimer.class);
    this.repositorioCategoriaMock = mock(RepositorioCategoria.class);
    this.servicioReglaVencimientoMock = mock(ServicioReglaVencimiento.class);
    this.servicioTimer =
      new ServicioTimerImpl(
        repositorioTimerMock,
        repositorioCategoriaMock,
        servicioReglaVencimientoMock
      );
  }

  private Timer buildTimerCompleto(Long timerId) {
    ReglaVencimiento regla = new ReglaVencimiento();
    regla.setId(1L);

    Producto producto = new Producto();
    producto.setId(10L);

    Categoria categoria = new Categoria();
    categoria.setId(5L);

    Timer timer = new Timer();
    timer.setId(timerId);
    timer.setProducto(producto);
    timer.setCategoria(categoria);
    timer.setReglaVencimiento(regla);
    timer.setEstado(EstadoTimer.ACTIVO);
    timer.setFechaCreacion(OffsetDateTime.now().minusHours(1));
    timer.setFechaVencimiento(OffsetDateTime.now().plusDays(3));
    return timer;
  }

  private Timer buildTimerGenerado() {
    Timer nuevoTimer = new Timer();
    nuevoTimer.setId(99L);
    nuevoTimer.setFechaCreacion(OffsetDateTime.now());
    nuevoTimer.setFechaVencimiento(OffsetDateTime.now().plusDays(3));
    return nuevoTimer;
  }

  @Test
  public void queDevuelvaTodasLosTimersActivos() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    OffsetDateTime fechaVencimiento = fechaCreacion.plusDays(3);
    Categoria categoria = new Categoria("mccafe.png", true, "mccafe");
    categoria.setId(1L);
    Producto producto = new Producto();
    ReglaVencimiento regla = new ReglaVencimiento();
    Timer timer = new Timer(fechaCreacion, fechaVencimiento, "1AF34", producto, categoria, regla);
    timer.setId(1L);
    List<Timer> timersActivos = List.of(timer);
    when(repositorioTimerMock.obtenerTimersSegunEstado(categoria.getId(), EstadoTimer.ACTIVO))
      .thenReturn(timersActivos);

    List<TimerDTO> listaObtenida = this.servicioTimer.obtenerTimersActivos(categoria.getId());

    assertEquals(1, listaObtenida.size());
    verify(repositorioTimerMock, times(1))
      .obtenerTimersSegunEstado(categoria.getId(), EstadoTimer.ACTIVO);
    assertEquals(1L, listaObtenida.get(0).getId());
  }

  @Test
  public void queNoHagaNadaSiElTimerNoExiste() {
    when(repositorioTimerMock.buscarPorId(99L)).thenReturn(null);

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> this.servicioTimer.modificarEstado(99L, EstadoTimer.ELIMINADO)
    );

    assertTrue(excepcion.getMessage().contains("nulo"));
    verify(repositorioTimerMock, never()).guardar(any());
  }

  @Test
  public void queElTimerQuedeMarcadoComoVencidoSiYaVencio() {
    OffsetDateTime fechaVencimientoPasada = OffsetDateTime.now().minusHours(1);
    Timer timer = new Timer()
      .builder()
      .fechaCreacion(OffsetDateTime.now().minusHours(2))
      .fechaVencimiento(fechaVencimientoPasada)
      .build();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);

    servicioTimer.modificarEstado(1L, EstadoTimer.ELIMINADO);

    assertEquals(EstadoTimer.VENCIDO, timer.getEstado());
    verify(repositorioTimerMock).guardar(timer);
  }

  @Test
  public void queElTimerQuedeMarcadoComoEliminadoSiNoVencioTodavia() {
    OffsetDateTime fechaVencimientoFutura = OffsetDateTime.now().plusHours(1);
    Timer timer = new Timer()
      .builder()
      .fechaCreacion(OffsetDateTime.now())
      .fechaVencimiento(fechaVencimientoFutura)
      .build();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);

    servicioTimer.modificarEstado(1L, EstadoTimer.ELIMINADO);

    assertEquals(EstadoTimer.ELIMINADO, timer.getEstado());
    verify(repositorioTimerMock).guardar(timer);
  }

  @Test
  void deberiaRetornarListaDeDTOsConTimersActivos() {
    Timer timer = buildTimer(
      1L,
      "Producto A",
      "group-1",
      OffsetDateTime.of(LocalDateTime.of(2024, 1, 1, 10, 0), ZoneOffset.ofHours(-3)),
      OffsetDateTime.of(LocalDateTime.of(2024, 12, 31, 10, 0), ZoneOffset.ofHours(-3)),
      "Almacen Central"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(1L);

    TimerDTO dto = resultado.get(0);
    assertEquals(1, resultado.size());
    assertEquals(1L, dto.getId());
    assertEquals("Producto A", dto.getNombre());
    assertEquals("group-1", dto.getGroupId());
    assertEquals("Almacen Central", dto.getUbicacion());
    assertFalse(dto.getFechaCreacion().isEmpty());
    assertFalse(dto.getFechaVencimiento().isEmpty());
  }

  @Test
  void deberiaRetornarListaVaciaSiNoHayTimersActivos() {
    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(Collections.emptyList());

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(1L);

    assertEquals(0, resultado.size());
  }

  @Test
  void deberiaMappearMultiplesTimersCorrectamente() {
    List<Timer> timers = List.of(
      buildTimer(
        1L,
        "Producto A",
        "g-1",
        OffsetDateTime.now(),
        OffsetDateTime.now().plusDays(1),
        "Zona Norte"
      ),
      buildTimer(
        2L,
        "Producto B",
        "g-2",
        OffsetDateTime.now(),
        OffsetDateTime.now().plusDays(2),
        "Zona Sur"
      ),
      buildTimer(
        3L,
        "Producto C",
        "g-3",
        OffsetDateTime.now(),
        OffsetDateTime.now().plusDays(3),
        "Zona Este"
      )
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO)).thenReturn(timers);

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(1L);

    assertEquals(3, resultado.size());
    assertEquals(1L, resultado.get(0).getId());
    assertEquals(2L, resultado.get(1).getId());
    assertEquals(3L, resultado.get(2).getId());
  }

  @Test
  void deberiaUsarNombreDefaultCuandoProductoEsNulo() {
    Timer timer = buildTimer(
      1L,
      null,
      "g-1",
      OffsetDateTime.now(),
      OffsetDateTime.now().plusDays(1),
      "Zona Norte"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(1L);

    assertEquals("producto desconocido", resultado.get(0).getNombre().toLowerCase());
  }

  @Test
  void deberiaUsarUbicacionDefaultCuandoReglaEsNula() {
    Timer timer = buildTimer(
      1L,
      "Producto A",
      "g-1",
      OffsetDateTime.now(),
      OffsetDateTime.now().plusDays(1),
      null
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(1L);

    assertEquals("general", resultado.get(0).getUbicacion().toLowerCase());
  }

  @Test
  void deberiaFormatearFechaVacíaCuandoEsNula() {
    Timer timer = buildTimer(1L, "Producto A", "g-1", null, null, "Zona Norte");

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(1L);

    assertTrue(resultado.get(0).getFechaCreacion().isEmpty());
    assertTrue(resultado.get(0).getFechaVencimiento().isEmpty());
  }

  @Test
  void deberiaLanzarExcepcionCuandoIdEsNulo() {
    IdInvalido excepcion = assertThrows(
      IdInvalido.class,
      () -> servicioTimer.obtenerTimersActivos(null)
    );
    assertTrue(excepcion.getMessage().contains("nulo"));
    verifyNoInteractions(repositorioTimerMock);
  }

  @Test
  void deberiaLanzarExcepcionCuandoIdEsCero() {
    IdInvalido excepcion = assertThrows(
      IdInvalido.class,
      () -> servicioTimer.obtenerTimersActivos(0L)
    );
    assertTrue(excepcion.getMessage().contains("positivo"));
    verifyNoInteractions(repositorioTimerMock);
  }

  @Test
  void deberiaLanzarExcepcionCuandoIdEsNegativo() {
    IdInvalido excepcion = assertThrows(
      IdInvalido.class,
      () -> servicioTimer.obtenerTimersActivos(-1L)
    );
    assertTrue(excepcion.getMessage().contains("positivo"));
    verifyNoInteractions(repositorioTimerMock);
  }

  @Test
  void deberiaLanzarExcepcionCuandoRepositorioRetornaNull() {
    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO)).thenReturn(null);

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("null"));
  }

  @Test
  void deberiaLanzarExcepcionCuandoRepositorioFalla() {
    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenThrow(new RuntimeException("Fallo la conexion con la base de datos"));

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("Fallo la conexion con la base de datos"));
  }

  @Test
  void deberiaProcesarConIdMaximo() {
    when(repositorioTimerMock.obtenerTimersSegunEstado(Long.MAX_VALUE, EstadoTimer.ACTIVO))
      .thenReturn(Collections.emptyList());

    assertDoesNotThrow(() -> servicioTimer.obtenerTimersActivos(Long.MAX_VALUE));
  }

  @Test
  void deberiaProcesarConIdMinimo() {
    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(Collections.emptyList());

    assertDoesNotThrow(() -> servicioTimer.obtenerTimersActivos(1L));
  }

  @Test
  void deberiaProcesarListaConUnSoloTimer() {
    Timer timer = buildTimer(
      99L,
      "Solo Producto",
      "g-99",
      OffsetDateTime.now(),
      OffsetDateTime.now().plusHours(1),
      "Deposito"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(99L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(99L);

    assertTrue(resultado.size() == 1);
  }

  @Test
  void deberiaManejarGroupIdNulo() {
    Timer timer = buildTimer(
      1L,
      "Producto",
      null,
      OffsetDateTime.now(),
      OffsetDateTime.now().plusDays(1),
      "Zona"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(1L);

    assertEquals(null, resultado.get(0).getGroupId());
  }

  @Test
  void deberiaRechazarNombreConXSS() {
    Timer timer = buildTimer(
      1L,
      "<script>alert('xss')</script>",
      "g-1",
      OffsetDateTime.now(),
      OffsetDateTime.now().plusDays(1),
      "Zona Norte"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("caracteres inválidos"));
  }

  @Test
  void deberiaRechazarUbicacionConSQLInjection() {
    Timer timer = buildTimer(
      1L,
      "Producto Seguro",
      "g-1",
      OffsetDateTime.now(),
      OffsetDateTime.now().plusDays(1),
      "' OR '1'='1'; DROP TABLE timers;--"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("caracteres inválidos"));
  }

  @Test
  void deberiaRechazarNombreConHTMLInjection() {
    Timer timer = buildTimer(
      1L,
      "<b>Bold</b>",
      "g-1",
      OffsetDateTime.now(),
      OffsetDateTime.now().plusDays(1),
      "Zona"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("caracteres inválidos"));
  }

  @Test
  void deberiaRechazarPayloadMasivoEnNombre() {
    String nombreMasivo = "A".repeat(10_000);
    Timer timer = buildTimer(
      1L,
      nombreMasivo,
      "g-1",
      OffsetDateTime.now(),
      OffsetDateTime.now().plusDays(1),
      "Zona"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("longitud máxima"));
  }

  @Test
  void deberiaRechazarIdNegativoExtremo() {
    assertThrows(IdInvalido.class, () -> servicioTimer.obtenerTimersActivos(Long.MIN_VALUE));

    verifyNoInteractions(repositorioTimerMock);
  }

  @Test
  void deberiaRechazarUbicacionConComandosShell() {
    Timer timer = buildTimer(
      1L,
      "Producto",
      "g-1",
      OffsetDateTime.now(),
      OffsetDateTime.now().plusDays(1),
      "zona; rm -rf /"
    );

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, EstadoTimer.ACTIVO))
      .thenReturn(List.of(timer));

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("caracteres inválidos"));
  }

  @Test
  @DisplayName(
    "HAP-03 | importarTimer | Importar timer a categoría distinta devuelve CategoriaDto correcta"
  )
  public void importarTimerACategoriaDistintaDeberiaRetornarCategoriaDto() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    Categoria categoriaOrigen = new Categoria("cocina.png", true, "Cocina");
    categoriaOrigen.setId(1L);
    Categoria categoriaDestino = new Categoria("isla.png", true, "Isla");
    categoriaDestino.setId(2L);
    Timer timer = new Timer(
      fechaCreacion,
      fechaCreacion.plusHours(2),
      "GROUP-01",
      new Producto(),
      categoriaOrigen,
      new ReglaVencimiento()
    );
    timer.setId(1L);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(repositorioCategoriaMock.buscarPorId(2L)).thenReturn(categoriaDestino);
    when(repositorioTimerMock.existeTimerActivoEnCategoriaYGrupo(2L, "GROUP-01")).thenReturn(false);

    CategoriaDto resultado = servicioTimer.importarTimer(1L, 2L);

    assertNotNull(resultado);
    assertEquals("Isla", resultado.getNombre());
    verify(repositorioTimerMock, times(1)).guardar(any(Timer.class));
  }

  @Test
  @DisplayName("HAP-04 | importarTimer | El clon guardado tiene la categoría destino asignada")
  public void importarTimerDeberiaGuardarClonConCategoriaDestino() {
    OffsetDateTime fechaCreacion = OffsetDateTime.now();
    Categoria categoriaOrigen = new Categoria("cocina.png", true, "Cocina");
    categoriaOrigen.setId(1L);
    Categoria categoriaDestino = new Categoria("isla.png", true, "Isla");
    categoriaDestino.setId(2L);
    Timer timer = new Timer(
      fechaCreacion,
      fechaCreacion.plusHours(2),
      "GROUP-01",
      new Producto(),
      categoriaOrigen,
      new ReglaVencimiento()
    );
    timer.setId(1L);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(repositorioCategoriaMock.buscarPorId(2L)).thenReturn(categoriaDestino);
    when(repositorioTimerMock.existeTimerActivoEnCategoriaYGrupo(2L, "GROUP-01")).thenReturn(false);

    servicioTimer.importarTimer(1L, 2L);

    ArgumentCaptor<Timer> captor = ArgumentCaptor.forClass(Timer.class);
    verify(repositorioTimerMock).guardar(captor.capture());
    assertEquals(categoriaDestino, captor.getValue().getCategoria());
  }

  @Test
  @DisplayName(
    "NEG-03 | importarTimer | Timer ya importado a la categoría lanza ValidacionException"
  )
  public void importarTimerYaImportadoDeberiaLanzarExcepcion() {
    Categoria categoriaOrigen = new Categoria("cocina.png", true, "Cocina");
    categoriaOrigen.setId(1L);
    Categoria categoriaDestino = new Categoria("isla.png", true, "Isla");
    categoriaDestino.setId(2L);
    Timer timer = new Timer(
      OffsetDateTime.now(),
      OffsetDateTime.now().plusHours(1),
      "GROUP-01",
      new Producto(),
      categoriaOrigen,
      new ReglaVencimiento()
    );
    timer.setId(1L);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(repositorioCategoriaMock.buscarPorId(2L)).thenReturn(categoriaDestino);
    when(repositorioTimerMock.existeTimerActivoEnCategoriaYGrupo(2L, "GROUP-01")).thenReturn(true);

    ValidacionException ex = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.importarTimer(1L, 2L)
    );
    assertEquals("El timer ya fue importado a esta categoría", ex.getMessage());
    verify(repositorioTimerMock, never()).guardar(any());
  }

  @Test
  @DisplayName(
    "NEG-04 | importarTimer | Importar timer a su misma categoría lanza ValidacionException"
  )
  public void importarTimerAMismaCategoriaDeberiaLanzarExcepcion() {
    Categoria categoria = new Categoria("cocina.png", true, "Cocina");
    categoria.setId(1L);
    Timer timer = new Timer(
      OffsetDateTime.now(),
      OffsetDateTime.now().plusHours(1),
      "GROUP-01",
      new Producto(),
      categoria,
      new ReglaVencimiento()
    );
    timer.setId(1L);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(repositorioCategoriaMock.buscarPorId(1L)).thenReturn(categoria);

    ValidacionException ex = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.importarTimer(1L, 1L)
    );
    assertEquals("El timer ya pertenece a esta categoría", ex.getMessage());
    verify(repositorioTimerMock, never()).guardar(any());
  }

  @Test
  @DisplayName("NEG-05 | importarTimer | Timer inexistente lanza ValidacionException")
  public void importarTimerInexistenteDeberiaLanzarExcepcion() {
    when(repositorioTimerMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(ValidacionException.class, () -> servicioTimer.importarTimer(99L, 2L));
    verify(repositorioTimerMock, never()).guardar(any());
  }

  @Test
  @DisplayName(
    "EDGE-01 | importarTimer | GroupId nulo en timer no rompe la validación de duplicados"
  )
  public void importarTimerConGroupIdNuloDeberiaFuncionar() {
    Categoria categoriaOrigen = new Categoria("cocina.png", true, "Cocina");
    categoriaOrigen.setId(1L);
    Categoria categoriaDestino = new Categoria("isla.png", true, "Isla");
    categoriaDestino.setId(2L);
    Timer timer = new Timer(
      OffsetDateTime.now(),
      OffsetDateTime.now().plusHours(1),
      (String) null,
      new Producto(),
      categoriaOrigen,
      new ReglaVencimiento()
    );
    timer.setId(1L);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(repositorioCategoriaMock.buscarPorId(2L)).thenReturn(categoriaDestino);
    when(repositorioTimerMock.existeTimerActivoEnCategoriaYGrupo(2L, null)).thenReturn(false);

    CategoriaDto resultado = servicioTimer.importarTimer(1L, 2L);

    assertNotNull(resultado);
    verify(repositorioTimerMock, times(1)).guardar(any(Timer.class));
  }

  @Test
  @DisplayName("HAP-05 | buscarPorId | Timer existente devuelve el timer correcto")
  public void buscarPorIdExistenteDeberiaRetornarTimer() {
    Timer timer = new Timer(
      OffsetDateTime.now(),
      OffsetDateTime.now().plusHours(1),
      "GROUP-01",
      new Producto(),
      new Categoria(),
      new ReglaVencimiento()
    );
    timer.setId(1L);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);

    Timer resultado = servicioTimer.buscarPorId(1L);

    assertNotNull(resultado);
    assertEquals(1L, resultado.getId());
    verify(repositorioTimerMock, times(1)).buscarPorId(1L);
  }

  @Test
  @DisplayName("NEG-06 | buscarPorId | Timer inexistente lanza ValidacionException")
  public void buscarPorIdInexistenteDeberiaLanzarExcepcion() {
    when(repositorioTimerMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(ValidacionException.class, () -> servicioTimer.buscarPorId(99L));
  }

  @Test
  @DisplayName(
    "EDGE-02 | buscarPorId | Id con valor Long.MAX_VALUE delega correctamente al repositorio"
  )
  public void buscarPorIdConValorMaximoDeberiaDelagarAlRepositorio() {
    when(repositorioTimerMock.buscarPorId(Long.MAX_VALUE)).thenReturn(null);

    assertThrows(ValidacionException.class, () -> servicioTimer.buscarPorId(Long.MAX_VALUE));
    verify(repositorioTimerMock, times(1)).buscarPorId(Long.MAX_VALUE);
  }

  private Timer buildTimer(
    Long id,
    String nombreProducto,
    String groupId,
    OffsetDateTime fechaCreacion,
    OffsetDateTime fechaVencimiento,
    String ubicacion
  ) {
    Timer timer = new Timer();
    timer.setId(id);
    timer.setGroupId(groupId);
    timer.setFechaCreacion(fechaCreacion);
    timer.setFechaVencimiento(fechaVencimiento);

    if (nombreProducto != null) {
      Producto producto = new Producto();
      producto.setNombre(nombreProducto);
      timer.setProducto(producto);
    }

    if (ubicacion != null) {
      ReglaVencimiento regla = new ReglaVencimiento();
      regla.setUbicacion(ubicacion);
      timer.setReglaVencimiento(regla);
    }

    return timer;
  }

  // ─── HAPPY PATH ───────────────────────────────────────────────

  @Test
  @DisplayName("HP-01 | renovarTimer | Retorna DTO del nuevo timer correctamente")
  void renovarTimer_deberiaRetornarDTODelNuevoTimer() {
    Timer timer = buildTimerCompleto(1L);
    Timer nuevoTimer = buildTimerGenerado();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        1L,
        null,
        1
      )
    )
      .thenReturn(nuevoTimer);

    TimerDTO resultado = servicioTimer.renovarTimer(timer);

    assertNotNull(resultado);
    assertEquals(99L, resultado.getId());
  }

  @Test
  @DisplayName(
    "HP-02 | renovarTimer | Cambia estado del timer original a RENOVADO cuando no está vencido"
  )
  void renovarTimer_deberiaModificarEstadoARenoVado() {
    Timer timer = buildTimerCompleto(1L);
    Timer nuevoTimer = buildTimerGenerado();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        1L,
        null,
        1
      )
    )
      .thenReturn(nuevoTimer);

    servicioTimer.renovarTimer(timer);

    verify(repositorioTimerMock).guardar(argThat(t -> t.getEstado() == EstadoTimer.RENOVADO));
  }

  @Test
  @DisplayName("HP-03 | renovarTimer | Llama a generarVencimiento con los datos del timer original")
  void renovarTimer_deberiaLlamarGenerarVencimientoConDatosCorrectos() {
    Timer timer = buildTimerCompleto(1L);
    Timer nuevoTimer = buildTimerGenerado();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        1L,
        null,
        1
      )
    )
      .thenReturn(nuevoTimer);

    servicioTimer.renovarTimer(timer);

    verify(servicioReglaVencimientoMock)
      .generarVencimiento(timer.getProducto(), timer.getCategoria(), 1L, null, 1);
  }

  // ─── NEGATIVE PATH ────────────────────────────────────────────

  @Test
  @DisplayName("NP-01 | renovarTimer | Timer sin regla de vencimiento lanza ValidacionException")
  void renovarTimer_sinReglaVencimiento_deberiaLanzarValidacionException() {
    Timer timer = buildTimerCompleto(1L);
    timer.setReglaVencimiento(null);

    assertThrows(ValidacionException.class, () -> servicioTimer.renovarTimer(timer));
  }

  @Test
  @DisplayName("NP-02 | renovarTimer | Error en generarVencimiento propaga la excepcion")
  void renovarTimer_errorEnGenerarVencimiento_deberiaPropagarExcepcion() {
    Timer timer = buildTimerCompleto(1L);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        1L,
        null,
        1
      )
    )
      .thenThrow(new IllegalArgumentException("El producto no tiene regla de vencimiento"));

    assertThrows(IllegalArgumentException.class, () -> servicioTimer.renovarTimer(timer));
  }

  @Test
  @DisplayName(
    "NP-03 | renovarTimer | Timer no encontrado en modificarEstado lanza ValidacionException"
  )
  void renovarTimer_timerNoEncontradoEnModificarEstado_deberiaLanzarValidacionException() {
    Timer timer = buildTimerCompleto(1L);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(null);

    assertThrows(ValidacionException.class, () -> servicioTimer.renovarTimer(timer));
  }

  // ─── EDGE CASES ───────────────────────────────────────────────

  @Test
  @DisplayName("EC-01 | renovarTimer | Timer vencido cambia estado a VENCIDO en lugar de RENOVADO")
  void renovarTimer_timerVencido_deberiaSetearEstadoVencido() {
    Timer timer = buildTimerCompleto(1L);
    timer.setFechaVencimiento(OffsetDateTime.now().minusMinutes(1)); // ya venció
    Timer nuevoTimer = buildTimerGenerado();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        1L,
        null,
        1
      )
    )
      .thenReturn(nuevoTimer);

    servicioTimer.renovarTimer(timer);

    verify(repositorioTimerMock).guardar(argThat(t -> t.getEstado() == EstadoTimer.VENCIDO));
  }

  @Test
  @DisplayName(
    "EC-02 | renovarTimer | Timer con fecha vencimiento exactamente ahora se considera vencido"
  )
  void renovarTimer_fechaVencimientoAhora_deberiaConsiderarseVencido() {
    Timer timer = buildTimerCompleto(1L);
    timer.setFechaVencimiento(OffsetDateTime.now().minusNanos(1));
    Timer nuevoTimer = buildTimerGenerado();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        1L,
        null,
        1
      )
    )
      .thenReturn(nuevoTimer);

    servicioTimer.renovarTimer(timer);

    verify(repositorioTimerMock).guardar(argThat(t -> t.getEstado() == EstadoTimer.VENCIDO));
  }

  @Test
  @DisplayName(
    "EC-03 | renovarTimer | El nuevo timer generado tiene fechas posteriores a la creacion original"
  )
  void renovarTimer_nuevoTimer_deberiaHaberSidoGeneradoDespuesDelOriginal() {
    Timer timer = buildTimerCompleto(1L);
    Timer nuevoTimer = buildTimerGenerado();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        1L,
        null,
        1
      )
    )
      .thenReturn(nuevoTimer);

    TimerDTO resultado = servicioTimer.renovarTimer(timer);

    OffsetDateTime fechaCreacionDTO = OffsetDateTime.parse(resultado.getFechaCreacion());
    assertTrue(fechaCreacionDTO.isAfter(timer.getFechaCreacion()));
  }

  // ─── SECURITY CASES ───────────────────────────────────────────

  @Test
  @DisplayName("SC-01 | renovarTimer | No se puede renovar un timer con regla de id negativo")
  void renovarTimer_reglaConIdNegativo_deberiaLanzarExcepcion() {
    Timer timer = buildTimerCompleto(1L);
    ReglaVencimiento reglaInvalida = new ReglaVencimiento();
    reglaInvalida.setId(-1L);
    timer.setReglaVencimiento(reglaInvalida);

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        -1L,
        null,
        1
      )
    )
      .thenThrow(new IllegalArgumentException("El producto no tiene regla de vencimiento"));

    assertThrows(IllegalArgumentException.class, () -> servicioTimer.renovarTimer(timer));
  }

  @Test
  @DisplayName(
    "SC-02 | renovarTimer | No se puede renovar un timer de otro producto manipulando el objeto"
  )
  void renovarTimer_productoManipulado_deberiaUsarDatosDelTimerOriginal() {
    Timer timer = buildTimerCompleto(1L);
    Timer nuevoTimer = buildTimerGenerado();

    when(repositorioTimerMock.buscarPorId(1L)).thenReturn(timer);
    when(
      servicioReglaVencimientoMock.generarVencimiento(
        timer.getProducto(),
        timer.getCategoria(),
        1L,
        null,
        1
      )
    )
      .thenReturn(nuevoTimer);

    servicioTimer.renovarTimer(timer);

    verify(servicioReglaVencimientoMock)
      .generarVencimiento(
        eq(timer.getProducto()),
        eq(timer.getCategoria()),
        eq(1L),
        isNull(),
        eq(1)
      );
  }
}
