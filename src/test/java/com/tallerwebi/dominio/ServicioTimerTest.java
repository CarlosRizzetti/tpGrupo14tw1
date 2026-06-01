package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.Producto;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.excepcion.IdInvalido;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.services.ServicioTimerImpl;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioTimerTest {

  public RepositorioTimer repositorioTimerMock;
  public RepositorioCategoria repositorioCategoriaMock;
  public ServicioTimerImpl servicioTimer;

  @BeforeEach
  public void init() {
    this.repositorioTimerMock = mock(RepositorioTimer.class);
    this.repositorioCategoriaMock = mock(RepositorioCategoria.class);
    this.servicioTimer = new ServicioTimerImpl(repositorioTimerMock, repositorioCategoriaMock);
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
    when(repositorioTimerMock.obtenerTimersSegunEstado(categoria.getId(), "activo"))
      .thenReturn(timersActivos);

    List<TimerDTO> listaObtenida = this.servicioTimer.obtenerTimersActivos(categoria.getId());

    assertEquals(1, listaObtenida.size());
    verify(repositorioTimerMock, times(1)).obtenerTimersSegunEstado(categoria.getId(), "activo");
    assertEquals(1L, listaObtenida.get(0).getId());
  }

  @Test
  public void queNoHagaNadaSiElTimerNoExiste() {
    when(repositorioTimerMock.buscarPorId(99L)).thenReturn(null);

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> this.servicioTimer.modificarEstadoAEliminado(99L)
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

    servicioTimer.modificarEstadoAEliminado(1L);

    assertEquals("vencido", timer.getEstado());
    assertFalse(timer.getEstaActivo());
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

    servicioTimer.modificarEstadoAEliminado(1L);

    assertEquals("eliminado", timer.getEstado());
    assertFalse(timer.getEstaActivo());
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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

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
    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo"))
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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(timers);

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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

    List<TimerDTO> resultado = servicioTimer.obtenerTimersActivos(1L);

    assertEquals("general", resultado.get(0).getUbicacion().toLowerCase());
  }

  @Test
  void deberiaFormatearFechaVacíaCuandoEsNula() {
    Timer timer = buildTimer(1L, "Producto A", "g-1", null, null, "Zona Norte");

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

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
    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(null);

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("null"));
  }

  @Test
  void deberiaLanzarExcepcionCuandoRepositorioFalla() {
    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo"))
      .thenThrow(new RuntimeException("Fallo la conexion con la base de datos"));

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("Fallo la conexion con la base de datos"));
  }

  @Test
  void deberiaProcesarConIdMaximo() {
    when(repositorioTimerMock.obtenerTimersSegunEstado(Long.MAX_VALUE, "activo"))
      .thenReturn(Collections.emptyList());

    assertDoesNotThrow(() -> servicioTimer.obtenerTimersActivos(Long.MAX_VALUE));
  }

  @Test
  void deberiaProcesarConIdMinimo() {
    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo"))
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

    when(repositorioTimerMock.obtenerTimersSegunEstado(99L, "activo")).thenReturn(List.of(timer));

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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

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

    when(repositorioTimerMock.obtenerTimersSegunEstado(1L, "activo")).thenReturn(List.of(timer));

    ValidacionException excepcion = assertThrows(
      ValidacionException.class,
      () -> servicioTimer.obtenerTimersActivos(1L)
    );
    assertTrue(excepcion.getMessage().contains("caracteres inválidos"));
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
}
