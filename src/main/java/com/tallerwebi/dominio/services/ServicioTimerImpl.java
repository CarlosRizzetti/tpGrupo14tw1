package com.tallerwebi.dominio.services;

import com.tallerwebi.dominio.entity.Categoria;
import com.tallerwebi.dominio.entity.ReglaVencimiento;
import com.tallerwebi.dominio.entity.Timer;
import com.tallerwebi.dominio.entity.Usuario;
import com.tallerwebi.dominio.entity.enums.EstadoTimer;
import com.tallerwebi.dominio.excepcion.CantidadInvalidaException;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.dominio.interfaces.RepositorioCategoria;
import com.tallerwebi.dominio.interfaces.RepositorioTimer;
import com.tallerwebi.dominio.interfaces.ServicioImpresion;
import com.tallerwebi.dominio.interfaces.ServicioReglaVencimiento;
import com.tallerwebi.dominio.interfaces.ServicioTelegram;
import com.tallerwebi.dominio.interfaces.ServicioTimer;
import com.tallerwebi.dominio.utils.ImpresionHelper;
import com.tallerwebi.dominio.utils.ValidacionHelper;
import com.tallerwebi.presentacion.dto.CategoriaDto;
import com.tallerwebi.presentacion.dto.TimerDTO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioTimer")
@Transactional
public class ServicioTimerImpl implements ServicioTimer {

  private static final String TIMER = "timer";
  private static final String NOT_AVAILABLE = "N/A";
  private final ServicioReglaVencimiento servicioReglaVencimiento;
  private final ServicioTelegram servicioTelegram;
  private RepositorioTimer repositorioTimer;
  private RepositorioCategoria repositorioCategoria;
  private ServicioImpresion servicioImpresion;

  @Autowired
  public ServicioTimerImpl(
    RepositorioTimer repositorioTimer,
    RepositorioCategoria repositorioCategoria,
    ServicioReglaVencimiento servicioReglaVencimiento,
    ServicioImpresion servicioImpresion,
    ServicioTelegram servicioTelegram
  ) {
    this.repositorioTimer = repositorioTimer;
    this.repositorioCategoria = repositorioCategoria;
    this.servicioReglaVencimiento = servicioReglaVencimiento;
    this.servicioImpresion = servicioImpresion;
    this.servicioTelegram = servicioTelegram;
  }

  @Override
  public List<TimerDTO> obtenerTimersActivos(Long idCategoria) {
    ValidacionHelper.validarId(idCategoria);

    List<Timer> timers = ValidacionHelper.queLaListaNoSeaNull(
      repositorioTimer.obtenerTimersSegunEstado(idCategoria, EstadoTimer.ACTIVO),
      "obtenerTimersSegunEstado"
    );

    return timers.stream().map(this::mapearATimerDTO).collect(Collectors.toList());
  }

  @Override
  public List<TimerDTO> obtenerTodosLosTimers() {
    List<Timer> timers = repositorioTimer.obtenerTodosLosTimers();
    return timers.stream().map(this::mapearATimerDTO).collect(Collectors.toList());
  }

  @Override
  public List<TimerDTO> obtenerTimersConFiltro(EstadoTimer estado, Long categoriaId) {
    List<Timer> timers = repositorioTimer.obtenerTimersConFiltro(estado, categoriaId);
    return timers.stream().map(this::mapearATimerDTO).collect(Collectors.toList());
  }

  @Override
  public void modificarEstado(Long timerId, EstadoTimer estado) {
    Timer timer = repositorioTimer.buscarPorId(timerId);
    ValidacionHelper.queNoSeaNull(timer, TIMER);

    if (timer.getCicloVida().getFechaVencimiento().isBefore(OffsetDateTime.now())) {
      timer.setEstado(EstadoTimer.VENCIDO);
    } else {
      timer.setEstado(estado);
    }

    repositorioTimer.guardar(timer);

    if (estado == EstadoTimer.ELIMINADO) {
      org.springframework.security.core.Authentication auth =
        org.springframework.security.core.context.SecurityContextHolder
          .getContext()
          .getAuthentication();
      String nombreUsuario = (auth != null && auth.isAuthenticated())
        ? auth.getName()
        : "Usuario desconocido";
      String productoNombre = timer.getProducto() != null
        ? timer.getProducto().getNombre()
        : NOT_AVAILABLE;
      String categoriaNombre = timer.getCategoria() != null
        ? timer.getCategoria().getNombre()
        : NOT_AVAILABLE;
      servicioTelegram.enviarMensaje(
        String.format(
          "🗑️ El usuario %s eliminó el timer del producto '%s' (Categoría: %s).",
          nombreUsuario,
          productoNombre,
          categoriaNombre
        )
      );
    }
  }

  public void modificarCantidad(Long timerId, Integer cantidad) {
    Timer timer = repositorioTimer.buscarPorId(timerId);
    ValidacionHelper.queNoSeaNull(timer, TIMER);

    timer.setCantidadProducto(cantidad);
  }

  @Override
  public void descontarStock(Long timerId, Integer cantidad) {
    Timer timer = repositorioTimer.buscarPorId(timerId);
    ValidacionHelper.queNoSeaNull(timer, TIMER);

    if (cantidad == null || cantidad <= 0) {
      throw new CantidadInvalidaException("La cantidad a descontar debe ser positiva");
    }
    if (cantidad > timer.getCantidadProducto()) {
      throw new CantidadInvalidaException(
        "No se puede descontar más stock del disponible en el timer"
      );
    }

    int restante = timer.getCantidadProducto() - cantidad;
    timer.setCantidadProducto(restante);

    // Si el timer se agotó, transiciona a ELIMINADO (sale del pool de activos)
    if (restante == 0) {
      timer.setEstado(EstadoTimer.CONSUMIDO);
    }

    repositorioTimer.guardar(timer);
  }

  @Override
  public CategoriaDto importarTimer(
    Long timerId,
    Long categoriaId,
    Integer cantidad,
    Usuario usuario
  ) {
    Timer timer = obtenerTimerValidado(timerId);
    Categoria categoriaDestino = obtenerCategoriaValidada(categoriaId);

    validarImportacion(timer, categoriaId, cantidad);

    Timer clon = crearTimerConCantidadYCategoria(timer, cantidad, categoriaDestino, usuario);
    actualizarTimerOriginal(timer, timerId, cantidad);
    repositorioTimer.guardar(clon);
    ImpresionHelper.intentarImpresionDeVencimiento(clon, servicioImpresion);

    String nombreUsuario = obtenerNombreUsuario(usuario);
    String productoNombre = timer.getProducto() != null
      ? timer.getProducto().getNombre()
      : NOT_AVAILABLE;
    servicioTelegram.enviarMensaje(
      String.format(
        "📥 El usuario %s importó el timer del producto '%s' a la categoría '%s' (Cantidad: %d).",
        nombreUsuario,
        productoNombre,
        categoriaDestino.getNombre(),
        cantidad
      )
    );

    return new CategoriaDto(categoriaDestino);
  }

  private Timer obtenerTimerValidado(Long timerId) {
    Timer timer = repositorioTimer.buscarPorId(timerId);
    ValidacionHelper.queNoSeaNull(timer, "timer");
    return timer;
  }

  private Categoria obtenerCategoriaValidada(Long categoriaId) {
    Categoria categoria = repositorioCategoria.buscarPorId(categoriaId);
    ValidacionHelper.queNoSeaNull(categoria, "categoria de destino");
    return categoria;
  }

  private void validarImportacion(Timer timer, Long categoriaId, Integer cantidad) {
    if (timer.getCategoria().getId().equals(categoriaId)) {
      throw new ValidacionException("El timer ya pertenece a esta categoría");
    }
    if (repositorioTimer.existeTimerActivoEnCategoriaYGrupo(categoriaId, timer.getGroupId())) {
      throw new ValidacionException("El timer ya fue importado a esta categoría");
    }
    if (cantidad > timer.getCantidadProducto()) {
      throw new CantidadInvalidaException(
        "La cantidad a importar no puede ser mayor al stock actual del vencimiento"
      );
    }
  }

  private void actualizarTimerOriginal(Timer timer, Long timerId, Integer cantidad) {
    if (cantidad.equals(timer.getCantidadProducto())) {
      modificarEstado(timerId, EstadoTimer.IMPORTADO);
      modificarCantidad(timerId, 0);
    } else {
      modificarCantidad(timerId, timer.getCantidadProducto() - cantidad);
    }
  }

  @Override
  public Timer buscarPorId(Long id) {
    Timer timer = repositorioTimer.buscarPorId(id);
    ValidacionHelper.queNoSeaNull(timer, TIMER);
    return timer;
  }

  @Override
  public TimerDTO renovarTimer(Timer timer, Integer cantidad, Usuario usuario) {
    ReglaVencimiento regla = timer.getReglaVencimiento();
    ValidacionHelper.queNoSeaNull(regla, "Regla vencimiento");

    modificarEstado(timer.getId(), EstadoTimer.RENOVADO);
    Timer nuevoTimer = servicioReglaVencimiento.generarVencimiento(
      timer.getProducto(),
      timer.getCategoria(),
      regla.getId(),
      null,
      cantidad,
      usuario
    );

    String nombreUsuario = obtenerNombreUsuario(usuario);
    String productoNombre = timer.getProducto() != null
      ? timer.getProducto().getNombre()
      : NOT_AVAILABLE;
    String categoriaNombre = timer.getCategoria() != null
      ? timer.getCategoria().getNombre()
      : NOT_AVAILABLE;
    servicioTelegram.enviarMensaje(
      String.format(
        "🔄 El usuario %s renovó el timer del producto '%s' (Categoría: %s) por una cantidad de %d.",
        nombreUsuario,
        productoNombre,
        categoriaNombre,
        cantidad
      )
    );

    return mapearATimerDTO(nuevoTimer);
  }

  private String obtenerNombreProducto(Timer timer) {
    if (timer.getProducto() == null) return "Producto desconocido";
    String nombre = timer.getProducto().getNombre();
    ValidacionHelper.validarCampoSeguro(nombre, "nombre del producto");
    return nombre;
  }

  private String obtenerUbicacion(Timer timer) {
    if (timer.getReglaVencimiento() == null) return "General";
    String ubicacion = timer.getReglaVencimiento().getUbicacion();
    ValidacionHelper.validarCampoSeguro(ubicacion, "ubicacion del producto");
    return ubicacion;
  }

  private String formatearFecha(Object fecha) {
    return fecha != null ? fecha.toString() : "";
  }

  private TimerDTO mapearATimerDTO(Timer timer) {
    ValidacionHelper.queNoSeaNull(timer, TIMER);
    CategoriaDto categoria = new CategoriaDto(timer.getCategoria());
    return new TimerDTO(
      timer.getId(),
      timer.getEstado(),
      obtenerNombreProducto(timer),
      timer.getGroupId(),
      formatearFecha(timer.getCicloVida().getFechaCreacion()),
      formatearFecha(timer.getCicloVida().getFechaVencimiento()),
      obtenerUbicacion(timer),
      timer.getCantidadProducto(),
      timer.getUsuario().getNombre(),
      categoria
    );
  }

  private Timer crearTimerConCantidadYCategoria(
    Timer timer,
    Integer cantidad,
    Categoria categoria,
    Usuario usuario
  ) {
    Timer clon = new Timer(
      timer.getCicloVida().getFechaCreacion(),
      timer.getCicloVida().getFechaVencimiento(),
      timer.getGroupId(),
      timer.getProducto(),
      categoria,
      timer.getReglaVencimiento(),
      cantidad,
      usuario
    );

    if (timer.getCicloVida().getDescongelamiento() != null) {
      clon.getCicloVida().setDescongelamiento(timer.getCicloVida().getDescongelamiento());
    }

    return clon;
  }

  private String obtenerNombreUsuario(Usuario usuario) {
    if (usuario == null) {
      return "Usuario desconocido";
    }
    if (usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
      return usuario.getNombre();
    }
    if (usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
      return usuario.getEmail();
    }
    return "Usuario desconocido";
  }

  @Override
  public List<Timer> obtenerTimersActivosConStockPorProducto(Long idProducto) {
    return repositorioTimer.obtenerTimersActivosConStockPorProducto(idProducto);
  }
}
